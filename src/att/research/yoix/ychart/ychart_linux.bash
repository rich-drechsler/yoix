#!/bin/bash
#
# Author: Richard Drechsler - 2020
#
# A script that uses the Java application launcher to start the Yoix interpreter,
# which then runs a Yoix script that manages charting application called Ychart.
# It relies on bash specific techniques and probably makes implicit assumptions
# that it's being used on a Linux system. It's clearly overkill, and I apologize
# for that, but much of the overhead was copied from a template that I often use
# when I write more substantial bash scripts.
#
# The recognized command line options are:
#
#      -D number
#         Set the size of your screen to number inches when measured along
#         the diagonal. Useful many years ago when starting the interpreter,
#         but it may no longer be necessary. A number less than or equal to
#         zero disables the calculations that the Yoix interpreter uses to
#         estimate the resolution of your screen, which currently is the
#         default behavior.
#
#      -g Run Yoix in a mode that guarantees line number and source file
#         information will be included in all error messages. This imposes
#         a performance penalty on the Yoix interpreter, but it's a useful
#         tool when you need to debug a Yoix application.
#
#      -m factor
#         Scale the fonts rendered by the Yoix interpreter by factor, which
#         should be a positive floating point number and is 1.0 by default.
#
#      -O Run Yoix in optimized mode, which can improve performance by about
#         10%, but eliminates lots of useful debugging information that can
#         help track down mistakes when things go wrong.
#
#     -X arg
#         Passes the complete option -Xarg directly to the Java application
#         launcher.
#
#     --dump
#         Writes information that might help debug this script to standard
#         output and quits without starting the Yoix interpreter.
#
#     --help
#         Prints a usage and help message on standard output that's usually
#         built from the first contiguous block of comments in this program
#         and then exits.
#
#     --jar file
#         Use file as the Yoix jar file. The default is
#
#             ../lib/yoix.jar
#
#         which is found relative to where this script is installed.
#
#     --java prog
#         Use prog as the Java application launcher. The default comes from
#         the value assigned to shell variable YOIX_JAVA, if it's defined in
#         your environment, or whatever bash finds in your current PATH when
#         it looks for a program named java.
#
#     --ychart-home dir
#         Use dir as the Ychart home. Yoix scripts that are part of the Ychart
#         package are found in directory
#
#             dir/scripts
#
#         while data for canned applications that are part of Ychart can be
#         found in directory
#
#             dir/data
#
#         The default Ychart home is the same as the Yoix home directory.
#
#     --ychart-script file
#         Use file as the Yoix script file that runs the Ychart application.
#         The default is script file is
#
#             ../ychart/scripts/ychart.yx
#
#         which is found relative to where this script is installed, or the
#         file
#
#             dir/ychart/scripts/ychart.yx
#
#         if the --ychart-home option was used to set the Ychart home directory
#         to dir.
#
#     --yoix-home dir
#         Use dir as the Yoix home directory. The default is the parent of
#         the directory that contains this script.
#
# Most of the options just documented are directed at Java, the Yoix interpreter,
# or Ychart. Option parsing, at least by this script, stops at an optional "--" or
# the first non-option command line argument. Arguments that aren't consumed here
# are handed directly to the Ychart script that manages the data visualization.
#
# I tried to write the script so it would work on different platforms, like Linux
# and MacOS, that support bash.
#         

ORIGINAL_PATH=$PATH
PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin

PROGRAM=$(basename "$0")
COMMANDLINE=("$0" "$@")
USAGE="Usage: $PROGRAM [options]... [--] ychart-script [script-options]... [data-file]"
HELP=
OPTION_CONSUMED=0

#
# Variables that we use to run the Yoix interpreter. Most can be set or modified
# using command line options - defaults, when needed, are assigned in Initialize.
# Decided, at least for now, to not grab defaults from the environment.
#

DUMP=FALSE
PROJECT_HOME=
JAVA=${YOIX_JAVA}
JAVA_OPTIONS="-Xmx1000m -Xms512m"
YOIX_HOME=
YOIX_OPTIONS=-m1.00
YCHART_SCRIPT=
YCHART_HOME=
YCHART_IMPLIED_ARGUMENT=
YCHART_IMPLIED_ARGUMENT_TAG=

Arguments() {

    if [ -n "$YCHART_IMPLIED_ARGUMENT" -a "$#" -eq 0 ] || [ -z "$YCHART_IMPLIED_ARGUMENT" -a $# -gt 0 ]
        then
            #
            # Doing this now so we can easily quote it in the actual command line.
            #
            if [ "$YCHART_IMPLIED_ARGUMENT" ]; then
                set -- "$YCHART_IMPLIED_ARGUMENT"
            fi

            if [ "$DUMP" = "TRUE" ]
                then
                    echo
                    echo "==== Command Line ===="
                    echo CLASSPATH="\"${YOIX_JAR}\"" "\"${JAVA}\"" ${JAVA_OPTIONS} -D"\"yoix.home=${YOIX_HOME}\"" att.research.yoix.YoixMain ${YOIX_OPTIONS} "\"${YCHART_SCRIPT}\"" "$@"
                else CLASSPATH="${YOIX_JAR}" "${JAVA}" ${JAVA_OPTIONS} -D"yoix.home=${YOIX_HOME}" att.research.yoix.YoixMain ${YOIX_OPTIONS} "${YCHART_SCRIPT}" "$@"
            fi
        else
            if [ "$DUMP" = "TRUE" ]; then
                echo
            fi
            if [ "$YCHART_IMPLIED_ARGUMENT" ]
                then Error "no arguments currently allowed with implied argument $YCHART_IMPLIED_ARGUMENT"
                else Error "at least one command line argument is required"
            fi
    fi
}

CanonicalPath() {
    local IGNORE=FALSE
    local TARGET
    local CANONICAL
    local BASENAME
    local DIRNAME

    #
    # If we don't get an answer and this function was called with the -i option
    # no error message is printed and nothing is returned to the caller. Means
    # the caller can detect the error (an empty string) and decide what to do.
    #

    while [ $# -gt 0 ]; do
        case "$1" in
            -i) IGNORE=TRUE;;
             *) break;;
        esac
        shift
    done

    CANONICAL=$(readlink -mn -- "$1" 2>/dev/null)

    if [ -z "$CANONICAL" ]; then
        #
        # A second try if the first readlink call failed. We use readlink again
        # because the options may have caused the first call to fail, but this
        # time all we do is use readlink to resolve symlinks. This happens on
        # OSX, so the second try isn't completely pointless. After that we use
        # cd and pwd to try to canonicalize the path.
        #
        # Definitely a hack that won't always work, mostly because it assumes
        # we can change into a directory that's obtained from the target path.
        # Still, it's a resonable approach that expands the cases this function
        # can handle when readlink can't do everything (i.e., resolve symlinks
        # and canonicalize paths).
        #

        TARGET=$(readlink "$1" 2>/dev/null || echo -n "$1")
        if [ -d "$TARGET" -a -x "$TARGET" ]
            then
                CANONICAL=$(cd "$TARGET" && echo -n "$(pwd -P)")
            else
                BASENAME=$(basename "$TARGET")
                DIRNAME=$(dirname "$TARGET")
                if [ -d "$DIRNAME" -a -x "$DIRNAME" ]; then
                    CANONICAL=$(cd "$DIRNAME" && echo -n "$(pwd -P)/$BASENAME")
                fi
        fi

        if [ -z "$CANONICAL" ]; then
            if [ "$IGNORE" = "FALSE" ]; then
                Error "can't get canonical path for $1"
            fi
        fi
    fi

    echo -n "$CANONICAL"
}

Cleanup() {
    local STATUS=$?

    set +e      # just in case
    trap "" 0 1 2 3 15

    exit $STATUS
}

Error() {
    local MESSAGE="$*"

    #
    # The trap set after this script started means our exit call should trigger
    # a Cleanup call, but the trap is cleared in Cleanup so errors that happen
    # during Cleanup mean an immediate exit.
    #

    if [ "$MESSAGE" ]; then
        echo "$PROGRAM: $MESSAGE" >&2
    fi
    exit 1
}

FindThisScript() {
    local DIRNAME
    local BASENAME
    local SCRIPT

    #
    # Locates this script using a technique that only works in bash. The answer
    # is used to pick defaults for important directories and to generate a help
    # message by locating this script and extracting the first contiguous block
    # of comments.
    #
    # NOTE - right now this function generates an error message and then quits
    # if anything goes wrong.
    #

    if [ "${BASH_SOURCE[0]}" ]
        then
            DIRNAME=$(cd "$(dirname "${BASH_SOURCE[0]}")" 2>/dev/null && pwd -P)
            if [ -d "$DIRNAME" ]
                then
                    BASENAME=$(basename "${BASH_SOURCE[0]}")
                    if [ "$BASENAME" ]
                        then
                            SCRIPT="${DIRNAME}/${BASENAME}"
                            if [ -f "$SCRIPT" ]; then
                                echo -n "$(CanonicalPath "$SCRIPT")"
                            fi
                        else Error "can't determine script basename using ${BASH_SOURCE[0]}"
                    fi
                else Error "can't determine script dirname name using ${BASH_SOURCE[0]}"
            fi
        else Error "can't determine the full pathname of this script using the BASH_SOURCE array"
    fi
}

Help() {
    local SCRIPT
    local LINE
    local SHEBANG
    local COPYRIGHT
    local AUTHORS

    #
    # Extracts the first contiguous block of comments from this script, cleans
    # each line up a bit and then copies them to standard output. The assumption
    # is that the block of comments starting at line 1 is documentation that's
    # an appropriate response, after it's been cleaned up, to the user's --help
    # request.
    #
    # NOTE - there's definitely room for improvement here. For example, when we
    # drop a line (e.g., the copyright line) that was surrounded by blank lines
    # in the source file, then we might want to skip the blank line that follows
    # the dropped line. Not a big deal and at this point not worth the effort.
    #

    echo "$USAGE"
    if [ -z "$HELP" ]
        then
            SCRIPT=$(FindThisScript)
            if [ -r "$SCRIPT" ]; then
                while [ "$LINE" ] || IFS= read LINE; do
                    case "$LINE" in
                        "#!"*)
                            if [ -z "$SHEBANG" ]
                                then SHEBANG=$LINE
                                else break
                            fi;;

                        "# Copyright "*)
                            COPYRIGHT+="${COPYRIGHT:+\n}${LINE#'# '}";;

                        "# Author: "*|"# Authors: "*)
                            AUTHORS+="${AUTHORS:+\n}${LINE#'# '}";;

                        "# "*)
                            echo "${LINE#"# "}";;

                        "#"*)
                            echo "${LINE#"#"}";;

                         *) break;;
                    esac
                    LINE=
                done <"$SCRIPT"
                if [ "$AUTHORS" -o "$COPYRIGHT" ]; then
                    echo
                    if [ "$AUTHORS" ]; then
                        echo -e "$AUTHORS"
                    fi
                    if [ "$COPYRIGHT" ]; then
                        echo -e "$COPYRIGHT"
                    fi
                fi
            fi
        else echo "$HELP"
    fi

    exit 0              # always quit
}

Initialize() {
    #
    # Called after the options are processed and makes sure the important shell
    # variables are properly initialized. Values assigned to variables that are
    # passed on as options to Java or Yoix are not checked.
    #

    PROJECT_HOME=${PROJECT_HOME:-$(dirname "$(dirname "$(FindThisScript)")")}

    JAVA=${JAVA:-$(PATH=${ORIGINAL_PATH} type -p java)}
    YOIX_HOME=${YOIX_HOME:-$PROJECT_HOME}
    YCHART_HOME=${YCHART_HOME:-${YOIX_HOME}/ychart}
    YOIX_JAR=${YOIX_JAR:-${YOIX_HOME}/lib/yoix.jar}
    YCHART_SCRIPT=${YCHART_SCRIPT:-${YCHART_HOME}/scripts/ychart.yx}

    if [ "$YCHART_IMPLIED_ARGUMENT_TAG" ]; then
        YCHART_IMPLIED_ARGUMENT=${YCHART_HOME}/data/${YCHART_IMPLIED_ARGUMENT_TAG}_ychart.yx
    fi

    if [ "$DUMP" = "TRUE" ]
        then
            echo "==== Shell Variables ===="
            echo "PROJECT_HOME=$PROJECT_HOME"
            echo "JAVA=$JAVA"
            echo "YOIX_HOME=$YOIX_HOME"
            echo "YCHART_HOME=$YCHART_HOME"
            echo "YOIX_JAR=$YOIX_JAR"
            echo "YCHART_SCRIPT=$YCHART_SCRIPT"
            if [ "$YCHART_IMPLIED_ARGUMENT_TAG" ]; then
                echo "YCHART_IMPLIED_ARGUMENT_TAG=$YCHART_IMPLIED_ARGUMENT_TAG"
                echo "YCHART_IMPLIED_ARGUMENT=$YCHART_IMPLIED_ARGUMENT"
            fi
        else
            if [ -d "$PROJECT_HOME" ]
                then
                    if [ -d "$YOIX_HOME" ]
                        then
                            if [ -d "$YCHART_HOME" ]
                                then
                                    if [ -f "$YOIX_JAR" -a -r "$YOIX_JAR" ]
                                        then
                                            if [ -f "$YCHART_SCRIPT" -a -r "$YCHART_SCRIPT" ]
                                                then
                                                    if [ -z "$YCHART_IMPLIED_ARGUMENT" ] || [ -f "$YCHART_IMPLIED_ARGUMENT" -a -r "$YCHART_IMPLIED_ARGUMENT" ]
                                                        then
                                                            if [ ! -x "$JAVA" ]; then
                                                                Error "can't execute Java application launcher $JAVA"
                                                            fi
                                                        else Error "can't read implied Ychart argument $YCHART_IMPLIED_ARGUMENT"
                                                    fi
                                                else Error "can't read Ychart script $YCHART_SCRIPT"
                                            fi
                                        else Error "can't read Yoix jar file $YOIX_JAR"
                                    fi
                                else Error "Ychart home directory $YCHART_HOME does not exist"
                            fi
                        else Error "Yoix home directory $YOIX_HOME does not exist"
                    fi
                else Error "project home directory $PROJECT_HOME does not exist"
            fi
    fi
}

Main() {
    Setup
    Options "$@"
    shift $(($OPTION_CONSUMED))
    Initialize "$@"
    Arguments "$@"
}

Options() {
    local ARGC=$#

    #
    # Original option handling code relied on GNU getopt command, but it's not
    # available on all systems that include bash (e.g., MacOS), so all option
    # related code was rewritten and this function now just uses "brute force".
    #

    while [ $# -gt 0 ]; do
        case "$1" in
            --dump) DUMP=TRUE;;
            --help) Help;;
            --jar) YOIX_JAR=$2; shift;;
            --jar=*) YOIX_JAR=${1#--jar=};;
            --java) JAVA=$2; shift;;
            --java=*) JAVA=${1#--java=};;
            --ychart-home) YCHART_HOME=$2; shift;;
            --ychart-home=*) YCHART_HOME=${1#--ychart-home=};;
            --ychart-script) YCHART_SCRIPT=$2; shift;;
            --ychart-script=*) YCHART_SCRIPT=${1#--ychart-script=};;
            --yoix-home) YOIX_HOME=$2; shift;;
            --yoix-home=*) YOIX_HOME=${1#--yoix-home=};;

             -g) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;
             -m) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"-m$2"; shift;;
            -m*) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;
             -D) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"-D$2"; shift;;
            -D*) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;
             -O) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;
             -X) JAVA_OPTIONS+=${JAVA_OPTIONS:+' '}"-X$2"; shift;;
            -X*) JAVA_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;

            --) shift; break;;
            -*) Error "missing case for option $1";;
             *) break;;                     # should be unnecessary
        esac
        shift
    done

    OPTION_CONSUMED=$(($ARGC - $#))
}

Setup() {
    #
    # Any initialization that should happen before the command line options are
    # processed.
    #

    set -e
    trap "Cleanup" 0 1 2 3 15
    umask 022

    #
    # Setup for version that bases it's behavior on the name of this script.
    #

    if [[ "$PROGRAM" =~ ^ychart_([A-Za-z0-9]+)([.]?[a-z]+)?$ ]]; then
        YCHART_IMPLIED_ARGUMENT_TAG=${BASH_REMATCH[1]}
        YOIX_OPTIONS="-O"
        USAGE="Usage: $PROGRAM [options]..."

        #
        # Sufficient for now, but there's undoubtedly a more general way to
        # handle this.
        #
        if [ "$YCHART_IMPLIED_ARGUMENT_TAG" = "unicode" ]; then
            JAVA_OPTIONS="-Xmx2000m -Xms1000m"
        fi
    fi
}

Usage() {
    echo "$USAGE" >&2
    Error
}

#
# Run the script
#

Main "$@"

