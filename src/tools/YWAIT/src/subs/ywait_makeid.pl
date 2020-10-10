sub peer_makeid {
    my $time = time();
    my @flds;

    #
    # Order of the entries in the session id that we build can't change
    # unless you make changes elsewere too. Also notice that the name of
    # the user's operating system is last because it can contain spaces
    # (e.g., Windows 95). All other values should be OK, however we may
    # not have explicitly checked every one.
    #

    $USERPID = makepid($USER, $USERPID);

    $SESSIONID = GetKey($USER);
    $SESSIONID .= " " . $time;
    $SESSIONID .= " " . ($time + 60*$SESSIONIDTIMER);
    $SESSIONID .= " " . $USER;
    $SESSIONID .= " " . $GROUP;
    $SESSIONID .= " " . $REMOTE_HOST;
    $SESSIONID .= " " . $CLIENTID;
    $SESSIONID .= " " . $RUNSTATE;
    $SESSIONID .= " " . $USERPID;
    $SESSIONID .= " " . $USERFLAGS;
    $SESSIONID .= " " . $RELEASE;
    $SESSIONID .= " " . $VMRELEASE;
    $SESSIONID .= " " . PickTimeZone($USER_TZ);
    $SESSIONID .= " " . $COMMANDSET;
    $SESSIONID .= " " . $OSNAME;

    @flds = split(/\s+/, $SESSIONID, $SESSIONIDFIELDS + 1);

    if (@flds == $SESSIONIDFIELDS) {
	ParseSessionID($SESSIONID);
	if (!SaveSessionID($SESSIONID)) {
	    print "STATUS=" . unpack("H*", "internalerror") . "\n";
	    print "REASON=" . unpack("H*", "could not save session id") . "\n";
	    exit(1);
	}
    } else {
	print "STATUS=" . unpack("H*", "internalerror") . "\n";
	print "REASON=" . unpack("H*", "could not generate session id") . "\n";
	exit(1);
    }
    return(1);
}

return 1;
