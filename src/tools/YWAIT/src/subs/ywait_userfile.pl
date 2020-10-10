#
# args: -S separator -F base_filename -C content (only for writing; no -C => reading)
# base_filename can only contain alphanumerics, underscores and non-leading dots (.)
#
sub peer_userfile {
  my $separator = $SEPARATOR;
  my $writemode = 0;
  my $file = "";
  my $full;
  my $len;
  my $size;
  my $content;

  while ($#_ >= 0) {
    if ($_[0] eq "-C" && $#_ > 0) {
      shift();
      $content = $_[0];
      $writemode = 1;
    } elsif ($_[0] =~ /^-C./) {
      $content = substr($_[0],2);
      $writemode = 1;
    } elsif ($_[0] eq "-F" && $#_ > 0) {
      shift();
      $file = $_[0];
    } elsif ($_[0] =~ /^-F./) {
      $file = substr($_[0],2);
    } elsif ($_[0] eq "-S" && $#_ > 0) {
      shift();
      $separator = $_[0];
    } elsif ($_[0] =~ /^-S./) {
      $separator = substr($_[0],2);
    }
    shift();
  }

  WriteHeader("SEPARATOR", "$separator", "ARGCOUNT", 2);

  if ($file eq "") {
    print "0\n$separator\nNo file name supplied.";
    WriteLog("-d0", "userfile: No file name supplied.");
  } else {
    $full = $file;
    $full =~ s:^.*[/\\]::;
    $full =~ s:^[.]+::;
    $full =~ s:[^.A-Za-z0-9_-]+::g;

    $full = "$USERHOME/$full";

    if ($writemode) {
      $len = length($content);
      if ($len == 0) {
	unlink($full);
	print "1\n$SEPARATOR\n";
      } else {
	if (open(USERFILE, ">" . UntaintedPath($full))) {
	  print USERFILE $content;
	  close(USERFILE);
	  $size = -s $full;
	  print "1\n$separator\nWrote $size characters to file '$file' on server.\n";
	} else {
	  print "0\n$separator\nCould not write to file '$file' on server.";
	  WriteLog("-d0", "userfile: Could not write to file '$full' on server.");
	}
      }
    } else {
      if (($size = -s $full) && open(USERFILE, "< $full")) {
	read(USERFILE, $content, $size);
	close(USERFILE);
	print "1\n$separator\n$content";
      } elsif ($size == 0) {
	print "1\n$separator\n";
	WriteLog("-d4", "userfile: file '$full' was empty.");
      } else {
	print "0\n$separator\nCould not open file '$file' on server for reading.";
	WriteLog("-d0", "userfile: Could not open file '$full' on server for reading.");
      }
    }
  }
  return(0);
}

return 1;
