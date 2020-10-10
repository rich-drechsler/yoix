sub peer_senddebug {
    if (open(DLOG, ">>" . UntaintedPath($DEBUGLOG))) {
	printf DLOG ("[%s] %s %d %s%s\n", $USERDATE, $USER, $USERPID, $GROUP);
	print DLOG ($_[$#_] . "\n====================\n");
	close(DLOG);
    }
    WriteHeader();
    return(0);
}

return 1;
