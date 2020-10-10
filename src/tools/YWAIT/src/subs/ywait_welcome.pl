sub peer_welcome {
    #
    # This routine sets the status based on RUNSTATE as the first argument.
    # Here's the current interpretation:
    #
    #	0.0	Won't pass checkid, so we should rarely see this
    #	0.?	Same as above, except administrators can continue
    #	1.0	Quit when the motd screen is closed
    #	1.?	Same as above, except administrators can continue
    #	?.?	Normal mode
    #
    # So setting RUNSTATE to 0.0 or 1.0 is how you take the system down.
    # Difference between them is that 1.0 shows motd screen before tossing
    # users. Setting RUNSTATE to 0.1 or 1.1 allows normal access to all
    # users listed in ADMINISTRATORS and tosses everyone else. Most of the
    # code that supports 0.* is in checkid.
    #

    my $prev;
    my $crnt;
    my $size;
    my $content;
    my $enabled = "true";

    WriteHeader("SEPARATOR",$SEPARATOR,"ARGCOUNT",4);

    if ($RUNSTATE =~ /^[01][.]0/) {
	$enabled = "false";
    } elsif ($RUNSTATE =~ /^[01][.]/) {
	if (IsAdministrator($USER)) {
	    $enabled = "true";
	} else {
	    $enabled = "false";
	}
    } else {
	$enabled = "true";
    }

    print "$enabled\n";
    print "$SEPARATOR\n";

    if (access("-r",$USERLOGINS)) {
	if (open(ULOG, "< $USERLOGINS")) {
	    $prev = "";
	    $crnt = "";
	    while (<ULOG>) {
		s/ # .*//;
		if (/LOGI[ND]/) {
		    s/\s+$//;
		    $prev = $crnt;
		    $crnt = $_;
		}
	    }
	    close(ULOG);
	    if ($prev ne "") {
		print $prev . "\n";
	    }
	}
    }
    print "$SEPARATOR\n";

    if (($size = -s $USERPREFS) && open(PREFS, "< $USERPREFS")) {
	read(PREFS, $content, $size);
	close(PREFS);
	print unpack("H*", $content);
    }
    print "$SEPARATOR\n";

    if (access("-r",$MOTDFILE)) {
	if (open(MOTD, "< " . $MOTDFILE)) {
	    while (<MOTD>) {
		print;
	    }
	    close(MOTD);
	}
    }

    return(0);
}

return 1;
