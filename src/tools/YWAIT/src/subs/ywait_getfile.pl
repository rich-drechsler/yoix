sub peer_getfile {
    my @accessoptions;
    my $accopt = 0;
    my $exitstatus = 1;
    my $expression = "";
    my $title = "";
    my $reverse = 0;
    my $binary = 0;
    my $tail = -1;
    my $filename = "";
    my $filesize;
    my $content;
    my @lines;
    my $ac = 0;
    my $checksum;

    $accessoptions[$accopt++] = "-r";

    while ($#_ >= $ac) {
	if ($_[$ac] eq "+d") {
	    $accessoptions[$accopt++] = $_[$ac];
	} elsif ($_[$ac] eq "-e") {
	    $ac++;
	    $expression = $_[$ac];
	} elsif ($_[$ac] =~ /^-e/) {
	    $expression = substr($_[$ac],2);
	} elsif ($_[$ac] eq "-r") {
	    $reverse = 1;
	} elsif ($_[$ac] eq "-b") {
	    $binary = 1;
	} elsif ($_[$ac] eq "-t") {
	    $ac++;
	    $tail = $_[$ac];
	} elsif ($_[$ac] =~ /^-t/) {
	    $tail = substr($_[$ac],2);
	} elsif ($_[$ac] eq "-T") {
	    $ac++;
	    $title = $_[$ac];
	} elsif ($_[$ac] =~ /^-T/) {
	    $title = substr($_[$ac],2);
	} elsif ($_[$ac] eq "--") {
	    $ac++;
	    last;
	} elsif ($_[$ac] =~ /^-/) {
	    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the administrator.";
	    WriteLog("-d3","[getfile] ERROR: bad argument: $_[$ac]");
	    last;
	} else {
	    while ($#_ >= $ac) {
		if ($_[$ac] =~ /^-/) {
		    $WARNINGMESSAGE="Request failed due to a software error.\nPlease report this problem to the administrator.";
		    WriteLog("-d3","[getfile] ERROR: bad argument: $_[$ac]");
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
	WriteLog("-d4","[getfile] FYI: $filename");
	$accessoptions[$accopt] = $filename;
	if (access(@accessoptions)) {
	    if ($binary) {
		if (!(-f $filename && ($filesize = -s $filename))) {
		    $WARNINGMESSAGE="The download could not be performed due to an empty or missing file.\nPlease report this problem to the administrator.";
		    WriteLog("-d3","[getfile] ERROR: missing file: $filename");
		    $exitstatus = 1;

		    WriteHeader();
		} elsif (open(GFILE, "< " . $filename)) {
		    if ($filesize == read(GFILE, $content, $filesize)) {
			$checksum = crc32($content);
			WriteHeader("SEPARATOR",$SEPARATOR,"ARGCOUNT",3);
			print "$filesize\n$SEPARATOR\n";
			print "$checksum\n$SEPARATOR\n";
			print unpack("H*", $content) . "\n";
		    } else {
			$WARNINGMESSAGE="The download could not be performed due to an error reading the download file.\nPlease report this problem to the administrator.";
			WriteLog("-d3","[getfile] ERROR: file read error: $filename ($filesize)");
			$exitstatus = 1;

			WriteHeader();
		    }
		    close(JFILE);
		} else {
		    $WARNINGMESSAGE="The download could not be performed due to an error reading the download file.\nPlease report this problem to the administrator.";
		    WriteLog("-d3","[getfile] ERROR: could not open: $filename");
		    $exitstatus = 1;

		    WriteHeader();
		}
	    } else {
		WriteHeader();
		if ($title ne "") {
		    print "$title\n\n";
		}
		if (open(GFILE, "< " . $filename)) {
		    $exitstatus = 0;
		    if ($tail >= 0) {
			if ($tail > 0) {
			    my $i;

			    $ac = 0;
			    while (<GFILE>) {
				if ($expression) {
				    eval $expression;
				}
				$lines[$ac++%$tail] = $_;
			    }
			    if ($reverse) {
				for ($i=(($ac-1)%$tail); $i>=0; $i--) {
				    print $lines[$i];
				}
				if ($ac > $tail) {
				    $ac = $ac%$tail;
				    for ($i=$tail-1; $i>=$ac; $i--) {
					print $lines[$i];
				    }
				}
			    } else {
				if ($ac > $tail) {
				    $ac = $ac%$tail;
				    for ($i=$ac; $i<$tail; $i++) {
					print $lines[$i];
				    }
				}
				for ($i=0; $i<$ac; $i++) {
				    print $lines[$i];
				}
			    }
			}
		    } elsif ($reverse) {
			$ac = 0;
			while (<GFILE>) {
			    if ($expression) {
				eval $expression;
			    }
			    $lines[$ac++] = $_;
			}
			while ($ac-- > 0) {
			    print $lines[$ac];
			}
		    } else {
			while (<GFILE>) {
			    if ($expression) {
				eval $expression;
			    }
			    print;
			}
		    }
		    close(GFILE);
		}
	    }
	} else {
	    WriteHeader();
	}
    } else {
	WriteHeader();
    }

    return($exitstatus);
}

return 1;
