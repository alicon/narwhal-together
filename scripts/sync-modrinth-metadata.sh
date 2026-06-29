#!/usr/bin/env bash
set -euo pipefail

: "${MODRINTH_TOKEN:?MODRINTH_TOKEN is required}"
: "${MODRINTH_PROJECT_ID:?MODRINTH_PROJECT_ID is required}"

api="https://api.modrinth.com/v2/project/${MODRINTH_PROJECT_ID}"
auth_header="Authorization: ${MODRINTH_TOKEN}"
user_agent="alicon/narwhal-together (github.com/alicon/narwhal-together)"

metadata="$({
	jq -n \
		--rawfile body docs/MODRINTH.md \
		'{
			title: "NARwhal Together",
			description: "Controller-friendly tools that make Minecraft easier and more fun for families playing together.",
			body: $body,
			categories: ["utility", "social"],
			client_side: "required",
			server_side: "required",
			license_id: "MIT",
			license_url: "https://github.com/alicon/narwhal-together/blob/main/LICENSE",
			source_url: "https://github.com/alicon/narwhal-together",
			issues_url: "https://github.com/alicon/narwhal-together/issues"
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
	--data-binary @src/main/resources/assets/narwhal_together/icon.png \
	"$api/icon?ext=png"

project_json="$(curl --fail-with-body --silent --show-error \
	--header "$auth_header" \
	--header "User-Agent: $user_agent" \
	"$api")"

if ! jq -e '.gallery[]? | select(.title == "Playing Together")' <<<"$project_json" >/dev/null; then
	curl --fail-with-body --silent --show-error \
		--request POST \
		--header "$auth_header" \
		--header "User-Agent: $user_agent" \
		--header "Content-Type: image/png" \
		--data-binary @docs/media/narwhal-together-banner.png \
		"$api/gallery?ext=png&featured=true&title=Playing%20Together&description=Three%20young%20adventurers%20regroup%20beneath%20the%20NARwhal%20Together%20mascot.&ordering=0"
fi

echo "Modrinth metadata synchronized for $MODRINTH_PROJECT_ID"
