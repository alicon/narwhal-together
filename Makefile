SHELL := /bin/zsh

GRADLE := GRADLE_USER_HOME=.gradle-user-home ./gradlew
MOD ?= cops-and-robbers
MC_VERSION ?= 1.21.11
MOD_VERSION := $(shell awk -F"'" '/mod_version =/{print $$2; exit}' mods/$(MOD)/build.gradle)
MOD_JAR := build/mods/$(MOD)/$(MC_VERSION)/libs/$(MOD)-$(MOD_VERSION).jar
MODRINTH_PROFILE ?= $(HOME)/Library/Application Support/ModrinthApp/profiles/Dad’s Minecraft
LIVE_TEST_MODS_DIR ?= $(or $(MODRINTH_LIVE_TEST_MODS_DIR),$(MODRINTH_PROFILE)/mods)
MODRINTH_VERSION_TYPE ?= alpha

.PHONY: help build build-all check test validate quick-validate api-docs format-check clean live-test preview-structures publish-modrinth deploy-modrinth sync-modrinth print-vars

help:
	@printf '%s\n' \
		'Common targets:' \
		'  make live-test                         Build MOD and copy its jar to Dad’s Minecraft.' \
		'  make preview-structures                Generate HTML previews for Cops and Robbers buildings.' \
		'  make build                             Build one mod. Default MOD=cops-and-robbers.' \
		'  make test                              Run tests for one mod.' \
		'  make check                             Run root Gradle check.' \
		'  make validate                          Run repo validation tasks.' \
		'  make quick-validate                    Run validation tasks that do not depend on versioned source layout.' \
		'  make build-all                         Build every mod subproject.' \
		'  make publish-modrinth                  Upload MOD to Modrinth using MODRINTH_TOKEN.' \
		'  make sync-modrinth                     Sync Modrinth project metadata using MODRINTH_TOKEN.' \
		'' \
		'Useful variables:' \
		'  MOD=narwhal-together|mushroom-the-yorkie|cops-and-robbers' \
		'  MC_VERSION=1.21.11' \
		'  LIVE_TEST_MODS_DIR=/path/to/profile/mods' \
		'  MODRINTH_LIVE_TEST_MODS_DIR=/path/to/profile/mods' \
		'  MODRINTH_PROFILE="$(HOME)/Library/Application Support/ModrinthApp/profiles/Dad’s Minecraft"' \
		'  MODRINTH_VERSION_TYPE=alpha|beta|release'

build:
	$(GRADLE) :$(MOD):build -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

build-all:
	$(GRADLE) buildAllMods -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

check:
	$(GRADLE) check -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

test:
	$(GRADLE) :$(MOD):test -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

validate:
	$(GRADLE) validateModLayout validateFormatting validateJavaFileSizes validateNoClientImportsInMain validatePublicApiDocs -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

quick-validate:
	$(GRADLE) validateFormatting validateJavaFileSizes validateNoClientImportsInMain -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

api-docs:
	$(GRADLE) validatePublicApiDocs -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

format-check:
	$(GRADLE) validateFormatting --no-daemon

clean:
	$(GRADLE) clean --no-daemon

live-test: build
	mkdir -p "$(LIVE_TEST_MODS_DIR)"
	tmp="$(LIVE_TEST_MODS_DIR)/.$(notdir $(MOD_JAR)).tmp"; \
	cp "$(MOD_JAR)" "$$tmp"; \
	mv -f "$$tmp" "$(LIVE_TEST_MODS_DIR)/$(notdir $(MOD_JAR))"
	@printf 'Copied %s to %s\n' "$(MOD_JAR)" "$(LIVE_TEST_MODS_DIR)"

preview-structures:
	python3 scripts/preview_cops_robbers_structures.py

publish-modrinth:
	MODRINTH_VERSION_TYPE=$(MODRINTH_VERSION_TYPE) $(GRADLE) :$(MOD):modrinth -Ptarget_minecraft_version=$(MC_VERSION) --no-daemon

deploy-modrinth: publish-modrinth

sync-modrinth:
	MODRINTH_MOD=$(MOD) scripts/sync-modrinth-metadata.sh

print-vars:
	@printf 'MOD=%s\n' "$(MOD)"
	@printf 'MC_VERSION=%s\n' "$(MC_VERSION)"
	@printf 'MOD_VERSION=%s\n' "$(MOD_VERSION)"
	@printf 'MOD_JAR=%s\n' "$(MOD_JAR)"
	@printf 'MODRINTH_PROFILE=%s\n' "$(MODRINTH_PROFILE)"
	@printf 'LIVE_TEST_MODS_DIR=%s\n' "$(LIVE_TEST_MODS_DIR)"
