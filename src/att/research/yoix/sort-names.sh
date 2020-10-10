#!/bin/sh
#
# This is a trivial maintenance script that's used to create a new, sorted list
# of "name" constants in YoixConstants.java.
#

awk '
	$0 ~ /\/\/.*/ {
		next
	}

	{
		if (NF > 1)
			print
	}
' $1 | sort -k3
