#!/bin/bash
#
# Author: Richard Drechsler - 2020
#
# A script that uses the Java application launcher to run the Yoix interpreter. It
# relies on bash specific techniques and probably also makes implicit assumptions
# that it's being used on a Linux system. It's clearly overkill, and I apologize
# for that, but much of the overhead was copied from a template that I often use
# when I write more substantial bash scripts.
#
# The recognized command line options are:
#
#      -d number
#         Set the Yoix interpreter's debug mode to number, which should be
#         a non-negative integer that's 0 by default. This option is really
#         only useful when the Yoix interpreter is reading from standard
#         input, and in that case -d1 is probably the most useful choice.
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
#     --yoix-home dir
#         Use dir as the Yoix home directory. The default is the parent of
#         the directory that contains this script.
#
#     --yoix-info
#         Asks the Yoix interpreter to print a detailed description of the
#         options that it supports on standard output and then exit. You
#         must use "--" on the command line before you can pass any options
#         directly to the Yoix interpreter, otherwise this script will try
#         to handle them and that usually will trigger an error.
#
#         The usage line printed by this option doesn't apply directly to
#         this script. Instead it assumes the Yoix interpreter is started
#         by a trivial one-line script that supplies some hardcoded options
#         to the Java application launcher and then points it at the Yoix jar
#         file. Always use the --help option to get information that applies
#         directly to this script.
#
# Most of the options just documented are directed at Java or the Yoix interpreter.
# Option parsing, at least by this script, stops at an optional "--" or the first
# non-option command line argument. Arguments that aren't consumed here are handed
# directly to the Yoix interpreter. Often there's just one unprocessed argument,
# and it's the path to a Yoix script that the interpreter is supposed to run.
#
# Any command line arguments that follow (the optional) script file are available
# for use by that script. If there's no script file mentioned on the command line
# the Yoix interpreter reads from standard input. Because everything that follows
# a "--" command line argument is handed to the Yoix interpreter, using "--" is
# another way to pass options directly to Yoix. For example, the command lines
#
#     yoix -O script.yx
#
# and
#
#    yoix -- -O script.yx
#
# accomplish exactly the same thing. In the first case the -O option is handled
# by this bash script and automatically passed to the Yoix interpreter, while in
# the second example the Yoix interpreter gets everything that follows the "--",
# which includes the -O option.
#
# I tried to write the script so it would work on different platforms, like Linux
# and MacOS, that support bash.
#

ORIGINAL_PATH=$PATH
PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin

PROGRAM=$(basename "$0")
COMMANDLINE=("$0" "$@")
USAGE="Usage: $PROGRAM [options]... [--] [yoix-options] [[[script] [script-options]...] [script-args]]"
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
JAVA_OPTIONS=-Xmx512m
YOIX_HOME=
YOIX_OPTIONS=-m1.00

Arguments() {
    if [ "$DUMP" = "TRUE" ]
        then
            echo
            echo "==== Command Line ===="
            echo CLASSPATH="\"${YOIX_JAR}\"" "\"${JAVA}\"" ${JAVA_OPTIONS} -D"\"yoix.home=${YOIX_HOME}\"" att.research.yoix.YoixMain ${YOIX_OPTIONS} "$@"
        else CLASSPATH="${YOIX_JAR}" "${JAVA}" ${JAVA_OPTIONS} -D"yoix.home=${YOIX_HOME}" att.research.yoix.YoixMain ${YOIX_OPTIONS} "$@"
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
    YOIX_HOME=${YOIX_HOME:-$PROJECT_HOME}
    YOIX_JAR=${YOIX_JAR:-${YOIX_HOME}/lib/yoix.jar}
    JAVA=${JAVA:-$(PATH=${ORIGINAL_PATH} type -p java)}

    if [ "$DUMP" = "TRUE" ]
        then
            echo "==== Shell Variables ===="
            echo "PROJECT_HOME=$PROJECT_HOME"
            echo "YOIX_HOME=$YOIX_HOME"
            echo "YOIX_JAR=$YOIX_JAR"
            echo "JAVA=$JAVA"
        else
            if [ -d "$PROJECT_HOME" ]
                then
                    if [ -d "$YOIX_HOME" ]
                        then
                            if [ -f "$YOIX_JAR" -a -r "$YOIX_JAR" ]
                                then
                                    if [ ! -x "$JAVA" ]; then
                                        Error "can't execute Java application launcher $JAVA"
                                    fi
                                else Error "can't read Yoix jar file $YOIX_JAR"
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
            --yoix-home) YOIX_HOME=$2; shift;;
            --yoix-home=*) YOIX_HOME=${1#--yoix-home=};;
            --yoix-info) YOIX_OPTIONS="--info";;

             -d) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"-d$2"; shift;;
            -d*) YOIX_OPTIONS+=${YOIX_OPTIONS:+' '}"$1";;
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
}

Usage() {
    echo "$USAGE" >&2
    Error
}

#
# Run the script
#

Main "$@"

