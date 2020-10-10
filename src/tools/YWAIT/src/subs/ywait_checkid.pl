sub peer_checkid {
    my $remote_host = $REMOTE_HOST;
    my $export = "FALSE";
    my $status = 6;	# completely invalid session id
    my $ac = 0;
    my $sid;
    my $time = time();
    my @flds;

    while ($#_ >= $ac) {
	if ($_[$ac] eq "+e") {
	    $export = "TRUE";
	} elsif ($_[$ac] eq "-e") {
	    $export = "FALSE";
	} elsif ($_[$ac] eq "-R") {
	    $ac++;
	    $remote_host = $_[$ac];
	} elsif ($_[$ac] =~ /^-R/) {
	    $remote_host = substr($_[$ac],2);
	} elsif ($_[$ac] =~ /^-/) {
	    print "STATUS=" . unpack("H*", "internalerror") . "\n";
	    $TMPTXT = sprintf("unexpected argument to checkid: %s", $_[$ac]);
	    print "REASON=" . unpack("H*", $TMPTXT) . "\n";
	    exit(1);
	} else {
	    last;
	}
	$ac++;
    }

    @flds = split(/\s+/, $_[$ac], $SESSIONIDFIELDS + 1);

    if (@flds == $SESSIONIDFIELDS) {
	$status = 5;
	if (ValidateSessionID($_[$ac], $flds[3], $flds[4], $flds[9])) {	# fatal error - for now
	    $status = 4;
	    if ($CHECKHOST eq "FALSE" || $flds[5] eq $remote_host) {
		$status = 3;
		if (ValidateHome($flds[3])) {
		    $status = 2;
		    if ($flds[1] <= $time && $time < $flds[1] + 60*$SESSIONIDTIMER) {
			if ($time < $flds[2] || $flds[2] == 0) {
			    if ($RUNSTATE eq $flds[7]) {
				if ($RUNSTATE eq "0.0") {
				    $status = 1;
				} elsif ($RUNSTATE =~ /^0[.]/) {
				    if (IsAdministrator($flds[3])) {
					$status = 0;
				    } else {
					$status = 1;
				    }
				} else {
				    $status = 0;
				}
			    } else {
				if ($RUNSTATE =~ /^[01][.]/) {
				    $status = 1;
				} else {
				    $status = 2;
				}
			    }
			}
		    }
		}
	    }
	}
    }

    if ($export eq "TRUE") {	# probably always want this
	$SESSIONID = $_[$ac];
	ParseSessionID($_[$ac]);
    }

    WriteLog("-d0","[Debug] status: $status");

    if ($status != 0) {
	WriteLog("-d0", "ADMIN: Removing sessionid [$_[$ac]] (checkid status=$status)");
	RemoveSessionID($_[$ac]);
    }

    return($status);
}

return 1;
