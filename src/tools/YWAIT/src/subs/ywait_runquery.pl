sub peer_runquery {

  my $exitstatus = 1;
  my $exitcode;
  my $argcount = 1;
  my $filesize;
  my $content;
  my $separator = $SEPARATOR;
  my $pid;
  my $input = "< /dev/null";
  my $tmp;
  my @sysargs;
  my @pipeline;

  sub fixer {
    $_[0] =~ s/'/'"'"'/go;
    return("'$_[0]'");
  }

  push @sysargs, ( split(/\s+/, $DBRQST) );

  if ($#_ >= 0) {
    push @sysargs, $_[0];
    shift();

    while ($#_ >= 0) {
      if ($_[0] eq "-A" && $#_ > 0) {
	shift();
	$argcount = $_[0];
      } elsif ($_[0] =~ /^-A./) {
	$argcount = substr($_[0],2);
      } elsif ($_[0] eq "-S" && $#_ > 0) {
	shift();
	$separator = $_[0];
      } elsif ($_[0] =~ /^-S./) {
	$separator = substr($_[0],2);
      } elsif ($_[0] eq "-Z" && $#_ > 0) {
	shift();
	$tmp = PipelineForKeywords($_[0]);
	if ($tmp ne "") {
	  push @pipeline, "|";
	  push @pipeline, fixer($tmp);
	}
      } elsif ($_[0] =~ /^-Z./) {
	$tmp = PipelineForKeywords(substr($_[0],2));
	if ($tmp ne "") {
	  push @pipeline, "|";
	  push @pipeline, fixer($tmp);
	}
      } elsif ($_[0] =~ /^--$/) {
	shift();
	last;
      } elsif ($_[0] =~ /^-.$/) {
	push @sysargs, $_[0];
      } elsif ($_[0] =~ /^-../) {
	push @sysargs, substr($_[0],0,2);
	push @sysargs, fixer(substr($_[0],2));
      } else {
	push @sysargs, fixer($_[0]); 
      }
      shift();
    }

    if ($#pipeline >= 0) {
      push @pipeline, "2>>";
      push @pipeline, $ERRFILE;
    }

    if ($#_ >= 0) {
      push @sysargs, "<";
      push @sysargs, $INPFILE; 
      $input = "< $INPFILE";
      if (open(INPUT, "> $INPFILE")) {
	while ($#_ >= 0) {
	  WriteLog("-d4","[runquery] FYI: $sysargs[0] input: $_[0]");
	  print INPUT shift() . "\n";
	}
	close(INPUT);
      } else {
	$input = "";
      }
    } else {
      push @sysargs, "<";
      push @sysargs, "/dev/null"; 
    }

    push @sysargs, "2>";
    push @sysargs, $ERRFILE;

    foreach $tmp ( @pipeline) {
      push @sysargs, $tmp;
    }

    push @sysargs, ">";
    push @sysargs, $OUTFILE;

    if ("$input" ne "") {
      WriteLog("-d0","[runquery] FYI: " . join(" ", @sysargs));

      $exitcode = system(@sysargs);

      if ($exitcode == -1) {
	$WARNINGMESSAGE = "The query failed to execute. Contact application support.";
	WriteLog("-d0","[runquery] ERROR: could not run the query: " . join(" ", @sysargs));
	WriteHeader();
      } elsif ($exitcode & 127) {
	$WARNINGMESSAGE = "The query execution terminated due to signal " . ($exitcode & 127) . ". Contact application support.";
	WriteLog("-d0","[runquery] ERROR: $sysargs[0] received signal " . ($exitcode & 127));
	WriteHeader();
      } else {
	$exitstatus = $exitcode >> 8;

	if (($filesize = -s $ERRFILE)) {
	  if (open(OUTPUT,"< " . $ERRFILE)) {
	    if (read(OUTPUT, $content, $filesize) != $filesize) {
	      close(OUTPUT);
	      $WARNINGMESSAGE="Query failed and could not read entire query error output.\nPlease report this problem to application support.";
	      WriteLog("-d0","[runquery] ERROR: incomplete read of query error output in $filename");
	      WriteHeader();
	    } else {
	      close(OUTPUT);
	      $WARNINGMESSAGE="Query failed on execution. Please report this problem to application support. The error message from the query is:\n$content";
	      WriteLog("-d1","[runquery] ERROR: query failed because: $content");
	      WriteHeader();
	    }
	  } else {
	    $WARNINGMESSAGE="Could not read the query output.\nPlease report this problem to application support.";
	    WriteLog("-d0","[runquery] ERROR: could not read query output in $filename");
	    WriteHeader();
	  }
	} elsif (($filesize = -s $OUTFILE)) {
	  if (open(OUTPUT,"< " . $OUTFILE)) {
	    if (read(OUTPUT, $content, $filesize) != $filesize) {
	      close(OUTPUT);
	      $WARNINGMESSAGE="Could not read entire query output.\nPlease report this problem to application support.";
	      WriteLog("-d0","[runquery] ERROR: incomplete read of query output in $filename");
	      WriteHeader();
	    } else {
	      close(OUTPUT);
	      WriteHeader("SEPARATOR",$separator,"ARGCOUNT",$argcount);
	      WriteLog("-d0","[runquery] FYI: output filesize was $filesize");
	      print $content;
	      $exitstatus = 0;
	    }
	  } else {
	    $WARNINGMESSAGE="Could not read the query output.\nPlease report this problem to application support.";
	    WriteLog("-d0","[runquery] ERROR: could not read query output in $filename");
	    WriteHeader();
	  }
	} else {
	  $WARNINGMESSAGE="FYI: Your request returned no output.";
	  WriteHeader();
	}
      }
    } else {
      $WARNINGMESSAGE="Could not run the query due to problems setting up the input.\nPlease report this problem to application support.";
      WriteLog("-d0","[runquery] ERROR: could not run the query $sysargs[0] because input $INPFILE could not be opened.");
      WriteHeader();
    }
  } else {
    $WARNINGMESSAGE="Could not run the query due to improper argument count.\nPlease report this problem to application support.";
    WriteLog("-d0","[runquery] ERROR: could not run the query due to missing arguments");
    WriteHeader();
  }

  return($exitstatus);
}

return 1;
