sub peer_logout {
    cleanuser($USER);
    if (open(ULOG, ">>" . UntaintedPath($USERLOGINS))) {
	print ULOG "$USERDATE from $REMOTE_HOST [LOGOUT $USERPID]\n";
	close(ULOG);
    }
    if ($LOGDETAIL == 1) {		# probably not recorded
	WriteLog("-d1", $COMMAND, $ARGCOUNT);
    }
    WriteHeader();
    RemoveSessionID($SESSIONID);
    return(0);
}

return 1;
