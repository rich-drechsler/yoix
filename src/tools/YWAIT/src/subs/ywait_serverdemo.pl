sub peer_serverdemo {
    my $exitstatus = 1;
    my $demonbr = 1;
    my $received = "";
    my $separator = $SEPARATOR;
    my $nonblank = 0;
    my $anbr = 0;
    my $ac = 0;
    my $n;

    while ($#_ >= $ac) {
	if ($_[$ac] eq "-d" && $#_ > $ac) {
	    $ac++;
	    $demonbr = $_[$ac];
	} elsif ($_[$ac] =~ /^-d./) {
	    $demonbr = substr($_[$ac],2);
	} elsif ($_[$ac] eq "-s" && $#_ > $ac) {
	    $ac++;
	    $separator = $_[$ac];
	} elsif ($_[$ac] =~ /^-s./) {
	    $separator = substr($_[$ac],2);
	} elsif ($_[$ac] eq "--") {
	    $ac++;
	    last;
	} elsif ($_[$ac] =~ /^-/) {
	    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the application administrator.";
	    WriteLog("-d3","[serverdemo] ERROR: bad argument: $_[$ac]");
	    last;
	} else {
	    $anbr = $ac;
	    while ($#_ >= $ac) {
		if ($_[$ac] =~ /^-/) {
		    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the application administrator.";
		    WriteLog("-d3","[serverdemo] ERROR: bad argument: $_[$ac]");
		    $received = "";
		    last;
		} else {
		    if (length($_[$ac]) > 0) {
		      $nonblank++;
		    }
		    $received .= "   Client arg " . ($ac - $anbr + 1) . ": $_[$ac]\n";
		}
		$ac++;
	    }
	    last;
	}
	$ac++;
    }

    if ($received ne "") {
	if ($demonbr >= 1 && $demonbr <= 2) {
	  $exitstatus = 0;
	  WriteHeader("SEPARATOR", $separator, "ARGCOUNT", $demonbr);
	  print "Of the 3 arguments requested, the server got\n\n$received\nas the " . $nonblank . " non-empty argument" . ($nonblank != 1 ? "s" : "") . " that you entered.";
	  if ($demonbr eq 2) {
	    print "$SEPARATOR\n";
	    $total = 0;
	    for ($n=$anbr; $n<=$#_; $n++) {
	      $total += length($_[$n]);
	    }
	    $cumul = 0;
	    for ($n=$anbr; $n<=$#_; $n++) {
	      $len = length($_[$n]);
	      $cumul += $len;
	      if ($total > 0) {
		printf STDOUT ("%d\t%d\t%f\t%f\t%s\n", ($n-$anbr+1), $len, ($len/$total), ($cumul/$total), $_[$n]);
	      } else {
		printf STDOUT ("%d\t%d\t%f\t%f\t%s\n", ($n-$anbr+1), 0, 1, 1, $_[$n]);
	      }
	    }
	  }
	} else {
	    $WARNINGMESSAGE="Request failed due to a unrecognized demo number ($demonbr).\nPlease report this problem to the application administrator.";
	    WriteLog("-d3","[serverdemo] ERROR: bad demo number: $demonbr");
	    WriteHeader();
	}
    } else {
	WriteHeader();
    }

    return($exitstatus);
}

return 1;
