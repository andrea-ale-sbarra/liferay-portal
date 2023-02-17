#!/bin/bash

function main {
	for dir in "./"*
	do
		if [ ${dir} = "./sample-workspace" ] ||
		   [ -f ${dir} ]
		then
			continue
		fi

		rsync -a --delete --exclude "client-extensions" --exclude "modules" --exclude "themes" sample-workspace/ ${dir}
	done
}

main "${@}"