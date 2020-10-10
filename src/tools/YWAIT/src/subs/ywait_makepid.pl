sub peer_makepid {
    my $user = $_[0];
    my $spid = $_[1];

    if ($spid == 0) {
	UserInit(); # sets $USERLOGINS
	if (-s $USERLOGINS && -r $USERLOGINS) {
	    if (open(ULOG, "< $USERLOGINS")) {
		while (<ULOG>) {
		    s/ # .*//;
		    if (!/LOGOUT/) {
			$spid++;
		    }
		}
		close(ULOG);
	    }
	}
	$spid++;
    }

    return($spid);
}

return 1;
