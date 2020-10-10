sub peer_access {
    my $allow_dotdot = 0;
    my $mode = 4;
    my $status = 0;
    my @statinfo;
    my $filename = "";
    my $ac = 0;

    while ($#_ >= $ac) {
	if ($_[$ac] eq "+d") {
	    $allow_dotdot = 1;
	} elsif ($_[$ac] eq "-r") {
	    $mode |= 4;
	} elsif ($_[$ac] eq "-w") {
	    $mode |= 2;
	} elsif ($_[$ac] eq "-x") {
	    $mode |= 1;
	} elsif ($_[$ac] =~ /^-/) {
	    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the administrator.";
	    WriteLog("-d3","[access] ERROR: bad argument: $_[$ac]");
	    last;
	} else {
	    while ($#_ >= $ac) {
		if ($_[$ac] =~ /^-/) {
		    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the administrator.";
		    WriteLog("-d3","[access] ERROR: bad argument: $_[$ac]");
		    $filename = "";
		    last;
		} else {
		    $filename .= $_[$ac];
		}
		$ac++;
	    }
	    last;
	}
	$ac++;
    }


    if ($filename ne "") {
	if ($allow_dotdot || index($filename,"..") < 0) {
	    if (-f $filename && (@statinfo = stat(_))) {
		if (($statinfo[2]&$mode) == $mode) {
		    $status = 1;
		} else {
		    $mode *= 8;
		    if (($statinfo[2]&$mode) == $mode) {
			$status = 1;
		    }
		}
	    }
	}
	WriteLog("-d4","[access] FYI: $filename (mode=$mode; status=$status)");
    }

    return($status == 1);
}

return 1;
