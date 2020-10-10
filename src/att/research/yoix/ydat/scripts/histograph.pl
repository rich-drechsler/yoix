#
# A Perl script that extracts data from an xdot file and then combines
# it with another data file in a format that can be displayed by YDAT.
# The ultimate goal is to have the nodes in the graph keep track of
# counts the way the bars in a histogram do and to provide the user
# with a visual display that conveys additional information about the
# selected and deselected records represented by each node (e.g., via
# partially filled nodes).
# 
# We assume you've used graphviz to generate an appropriate xdot graph
# and that you have also generated another data file, perhaps using a
# database query, that contains additional information about the nodes
# in your graph. Although not necessarily required, this script also
# currently assumes that each record in the data file has two fields
# that reference nodes in your graph. The indices of those fields is
# contained in the @datafields array (below) and can be changed using
# the -f command line option.
#
# If example.xdot is the name of the xdot file and example.data is the
# corresponding data file then you would run something like,
#
#    perl histograph.pl example.xdot example.data >histograph.data
#
# to generate a data file will work with the config_demo3.yx YDAT
# config file. The script assumes the xdot graph data comes first, so
# the order of the two input files is not optional. The full command
# line to run the data visualization might look something like:
#
#    perl histograph.pl example.xdot example.data | ydat +p -Cconfig_demo3.yx
#
# Unfortunately the +p option is currently required when you want ydat
# to read data from stdin. It's inconvenient and may be something that
# will change in a future release, but for now it's required in the
# last command line.
#
# As mentioned above, you can use the -f command line option to make
# sure the @datafields array contains the indices of the fields in the
# example.data file that reference the nodes in example.xdot.
#

my $datadelimiter = "|";	# used to delimit fields in the data
my @datafields = (1, 2);	# graph node names are found at these indices
my %graphelements;
my @nodetable;
my @edgetable;
my %defaultvalues;

#
# We first set some magic bits that are interpreted by the YDAT code
# that processes the intermediate graph format that we generate here
# and then hand to the low level YDAT Java code. There are more flags
# but we're not going to describe them here.
# 

my $nodeflags = 0x01;		# NODE bit
my $edgeflags = 0x02;		# EDGE bit

#
# These could be used to collect default values from the corresponding
# definitions that graphviz generates, but right now all we use it for
# is to eliminate them as possible node names.
#

%defaultvalues = (
    "graph" => "",
    "node" => "",
    "edge" => ""
);

#
# Simple minded descriptions of the node and edge attributes that we're
# looking for in the xdot file and information about how to translate
# the extracted attributes into the internal format that YDAT currently
# expects. These lists aren't necessarily complete and it's not hard to
# image better ways to handle things, but right now the descriptions are
# arranged in groups of four elements.
#

@nodetable = (
    "_type_",		# name
    0,			# type (1 means special text token output)
    "",			# prefix
    "",			# suffix

    "_draw_",
    0,
    "",
    "",

    "_ldraw_",
    0,
    "< ",
    ">",

    "color",
    1,
    "A 2 color S ",
    "",

    "fontcolor",
    1, 
    "A 2 fontcolor S ",
    "",

    "fontsize",
    0,
    "A 2 fontsize N 1 ",
    "",

    "fontstyle",
    1,
    "A 2 fontstyle S ",
    "",

    "style",
    0,
    "A 1 ",
    "",
);

@edgetable = (
    "_type_",		# name
    0,			# type (1 means special text token output)
    "",			# prefix
    "",			# suffix

    "_tdraw_",
    0,
    "w 2 ",
    "",

    "_draw_",
    0,
    "w 0 ",
    "",

    "_hdraw_",
    0,
    "w 1 ",
    "",

    "_ldraw_",
    0,
    "< ",
    ">",

    "color",
    1,
    "A 2 color S ",
    "",

    "fontcolor",
    1,
    "A 2 fontcolor S ",
    "",

    "fontsize",
    0,
    "A 2 fontsize N 1 ",
    "",

    "fontstyle",
    1,
    "A 2 fontstyle S ",
    "",

    "style",
    0,
    "A 1 ",
    "",
);

sub GetAttribute {
    my $name = shift;
    my $source = shift;
    my $value = "";

    if ($source =~ /$name=/) {
	$value = $source;
	$value =~ s/^.*$name=\s*//;
	if ($value =~ /^"[^"]*"/o) {
	    $value =~ s/^"([^"]*)".*$/$1/o;
	} elsif ($value =~ /[\s,]/o) {
	    $value =~ s/^([^\s,]*).*$/$1/o;
	} elsif ($value =~ /];$/o) {
	    $value =~ s/];$//o;
	}
    }
    return($value);
}

sub GetAttributes {
    my $name = shift;
    my $source = shift;
    my $attributes = shift;
    my $table = shift;
    my $attribute;
    my $sep;
    my $n;
    
    for ($n = 0; $n < @$table; $n += 4) {
	$attribute = GetAttribute($$table[$n], $source);
	if ($attribute ne "") {
	    if ($$table[$n+1] == 1) {
		$attribute = length($attribute) . " -" . $attribute;
	    }
	    $sep = ($attributes eq "" || $attributes =~ / $/o) ? "" : " ";
	    $attributes .= $sep . $$table[$n+2] . $attribute . $$table[$n+3];
	}
    }
    return($attributes);
}

sub Options {
    for (; @ARGV; shift(@ARGV)) {
	if ($ARGV[0] eq "-d") {
	    shift(@ARGV);
	    $datadelimiter = $ARGV[0];
	} elsif ($ARGV[0] =~ /^-d/) {
	    $datadelimiter = substr($ARGV[0], 2);
	} elsif ($ARGV[0] eq "-f") {
	    shift(@ARGV);
	    @datafields = split(/[,\s]\s*/, $ARGV[0]);
	} elsif ($ARGV[0] =~ /^-f/) {
	    @datafields = split(/[,\s]\s*/, substr($ARGV[0], 2));
	} elsif ($ARGV[0] =~ /^-.*/) {
	    printf(STDERR "$0: invalid option $ARGV[0] - quitting\n");
	    exit(1);
	} else {
	    last;
	}
    }

    if (@datafields != 2) {
	printf(STDERR "$0: you must supply two data field indices\n");
	exit(1);
    }
}

sub TranslateGraph {
    my $name;
    my $source;
    my @fields;

    #
    # Subroutine that tries to extract node and edge information from a
    # graph file and translate it into a format that can be handled by
    # the low level YDAT graph code. Results are written to stdout until
    # we read a line that equals "}", which we assume marks the end of
    # the graph data.
    # 

    while (<>) {
	chomp;
	if ($_ ne "}") {
	    $source = $_;
	    $source =~ s/^\s*//o;
	    if ($source =~ /^\w+ \[/o) {		# it's a node
		@fields = split(/ \[/o, $source, 2);
		$name = $fields[0];
		if (!exists($defaultvalues{$name})) {
		    $graphelements{$name} = GetAttributes($name, $fields[1], "$name $nodeflags $defaultvalues{node}", \@nodetable);
		} else {
		    if ($name eq "node") {
			$defaultvalues{$name} = GetAttributes($name, $fields[1], $defaultvalues{node}, \@nodetable);
		    } elsif ($name eq "edge") {
			$defaultvalues{$name} = GetAttributes($name, $fields[1], $defaultvalues{edge}, \@edgetable);
		    }
		}
	    } elsif ($source =~ /^\w+ -> \w+ \[/o) {	# it's an edge
		@fields = split(/ \[/o, $source, 2);
		$name = $fields[0];
		$name =~ s/\s//go;
		$graphelements{$name} = GetAttributes($name, $fields[1], "$name $edgeflags $defaultvalues{edge}", \@edgetable);
	    }
	} else {
	    last;
	}
    }
}

sub UpdateData {
    my @fields;
    my $edge;
    my $node1;
    my $node2;
    my $delim = $datadelimiter;

    while (<>) {
	chomp;
	@fields = split(/[$delim]/o, $_);
	if (@fields > 0) {
	    $node1 = $fields[$datafields[0]];
	    $node2 = $fields[$datafields[1]];
	    $edge = "$node1->$node2";
	    if (exists($graphelements{$node1})) {
		if (exists($graphelements{$node2})) {
		    if (exists($graphelements{$edge})) {
			#
			# Right now we have to output the full text for
			# nodes and edges if we want to accumulate the
			# information from different records. Probably
			# not hard to improve the behavior, but it may
			# need Java code changes - later.
			#
			printf("%s%s%s%s%s%s%s\n", $_, $delim, $graphelements{$node1}, $delim, $graphelements{$edge}, $delim, $graphelements{$node2});
		    } else {
			printf(STDERR "$0: can't find definition for edge %s\n", $edge);
			exit(1);
		    }
		} else {
		    printf(STDERR "$0: can't find definition for node %s\n", $node2);
		    exit(1);
		}
	    } else {
		printf(STDERR "$0: can't find definition for node %s\n", $node1);
		exit(1);
	    }
	}
    }
}

Options();
TranslateGraph();
UpdateData();

