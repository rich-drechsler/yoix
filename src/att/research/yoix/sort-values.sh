#!/bin/sh
#
# This is a trivial maintenance script that's used to create a new, sorted list
# of "value" constants in YoixConstants.java.
#

sort -k3 $1 | awk '
	BEGIN {
		count = 0;
	}

	$0 ~ /\/\/.*/ {
		next
	}

	{
		if (NF > 1)
			printf "    %s %s %s  %s = %d;\n", $1, $2, $3, $4, ++count
	}
'
