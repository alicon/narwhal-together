#!/usr/bin/env bash
set -euo pipefail

: "${MODRINTH_TOKEN:?MODRINTH_TOKEN is required}"

mod="${MODRINTH_MOD:-narwhal-together}"

case "$mod" in
	narwhal-together)
		: "${MODRINTH_PROJECT_ID:?MODRINTH_PROJECT_ID is required for narwhal-together}"
		project_id="$MODRINTH_PROJECT_ID"
		user_agent="alicon/narwhal-together (github.com/alicon/narwhal-together)"
		body_file="docs/MODRINTH.md"
		icon_file="mods/narwhal-together/src/main/resources/assets/narwhal_together/icon.png"
		title="NARwhal Together"
		description="Controller-friendly tools that make Minecraft easier and more fun for families playing together."
		categories='["utility", "social"]'
		license_url="https://github.com/alicon/narwhal-together/blob/main/LICENSE"
		source_url="https://github.com/alicon/narwhal-together"
		issues_url="https://github.com/alicon/narwhal-together/issues"
		gallery_specs=(
			"Playing Together|docs/media/narwhal-together-banner.png|Three young adventurers regroup beneath the NARwhal Together mascot.|true|0"
		)
		;;
	mushroom-the-yorkie)
		project_id="${MUSHROOM_MODRINTH_PROJECT_ID:-mushroom-the-yorkie}"
		user_agent="alicon/mushroom-the-yorkie (github.com/alicon/minecraft-mods)"
		body_file="docs/MODRINTH_MUSHROOM.md"
		icon_file="mods/mushroom-the-yorkie/src/main/resources/assets/mushroom_yorkie/icon.png"
		title="Mushroom the Yorkie"
		description="A tiny Yorkie companion with treats, naps, bathroom barks, and sheep-chasing opinions."
		categories='["mobs", "game-mechanics"]'
		license_url="https://github.com/alicon/minecraft-mods/blob/main/LICENSE"
		source_url="https://github.com/alicon/minecraft-mods"
		issues_url="https://github.com/alicon/minecraft-mods/issues"
		gallery_specs=(
			"Mushroom Wants a Treat|docs/media/mushroom-the-yorkie-banner.png|Mushroom watches for a treat beside the bed.|true|0"
			"Curled Up Indoors|docs/media/mushroom-the-yorkie-sleeping.png|At night indoors, Mushroom curls up and closes his eyes.|false|1"
			"Big Feelings About Cows|docs/media/mushroom-the-yorkie-mob-chase.png|Mushroom can bark at and chase peaceful mobs until called off with a treat.|false|2"
			"Adventure Companion|docs/media/mushroom-the-yorkie-adventure.png|A small companion for big family worlds.|false|3"
		)
		;;
	*)
		echo "Unknown MODRINTH_MOD '$mod'. Expected narwhal-together or mushroom-the-yorkie." >&2
		exit 2
		;;
esac

api="https://api.modrinth.com/v2/project/${project_id}"
auth_header="Authorization: ${MODRINTH_TOKEN}"

metadata="$({
	jq -n \
		--rawfile body "$body_file" \
		--arg title "$title" \
		--arg description "$description" \
		--argjson categories "$categories" \
		--arg license_url "$license_url" \
		--arg source_url "$source_url" \
		--arg issues_url "$issues_url" \
		'{
			title: $title,
			description: $description,
			body: $body,
			categories: $categories,
			client_side: "required",
			server_side: "required",
			license_id: "MIT",
			license_url: $license_url,
			source_url: $source_url,
			issues_url: $issues_url
		}'
})"

curl --fail-with-body --silent --show-error \
	--request PATCH \
	--header "$auth_header" \
	--header "User-Agent: $user_agent" \
	--header "Content-Type: application/json" \
	--data "$metadata" \
	"$api"

curl --fail-with-body --silent --show-error \
	--request PATCH \
	--header "$auth_header" \
	--header "User-Agent: $user_agent" \
	--header "Content-Type: image/png" \
	--data-binary @"$icon_file" \
	"$api/icon?ext=png"

project_json="$(curl --fail-with-body --silent --show-error \
	--header "$auth_header" \
	--header "User-Agent: $user_agent" \
	"$api")"

urlencode() {
	jq -nr --arg value "$1" '$value|@uri'
}

for spec in "${gallery_specs[@]}"; do
	IFS='|' read -r gallery_title gallery_file gallery_description featured ordering <<<"$spec"
	if jq -e --arg title "$gallery_title" '.gallery[]? | select(.title == $title)' <<<"$project_json" >/dev/null; then
		continue
	fi

	curl --fail-with-body --silent --show-error \
		--request POST \
		--header "$auth_header" \
		--header "User-Agent: $user_agent" \
		--header "Content-Type: image/png" \
		--data-binary @"$gallery_file" \
		"$api/gallery?ext=png&featured=${featured}&title=$(urlencode "$gallery_title")&description=$(urlencode "$gallery_description")&ordering=${ordering}"
done

echo "Modrinth metadata synchronized for $mod ($project_id)"
