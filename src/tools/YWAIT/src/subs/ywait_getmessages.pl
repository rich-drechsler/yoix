sub peer_getmessages {
    my $exitstatus = 1;
    my $needheader = 1;
    my $path;

    if (ValidateHome($USER)) {
	if (LockNamedFile(UntaintedPath($USERMESSAGES), 5)) {
	    if (-s $USERMESSAGES && -r $USERMESSAGES) {
		$path = "$USERMESSAGES.$$";
		#
		# Move it first so WriteHeader() thinks its really gone
		# and doesn't indicate that the user has messages. Small
		# kludge, but it's not a big deal.
		#
		if (rename($USERMESSAGES, $path)) {
		    $exitstatus = getfile($path);
		    unlink($path);
		    $needheader = 0;
		}
	    }
	    UnlockNamedFile(UntaintedPath($USERMESSAGES));
	}
    }

    if ($needheader) {
	WriteHeader();
    }

    return($exitstatus);
}

return 1;
