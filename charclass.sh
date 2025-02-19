#!/bin/bash
#
# Originally used something like this to build the HEX lists that correspond to
# various "charater classes" used as arguments in the ByteSelector recursive calls
# in the bytedump bash script. It was simple script that I didn't save, but when
# I looked more closely at the hex code definitions of character sets in the bash
# version (while working on a Java bytedump version) I wasn't convinced that all
# of them were correct (e.g., lower, upper, punct). However, using this script to
# generate the various HEX lists reproduces what's hardcoded in the bytedump bash
# script.
#
# Type
#
#     make charclass
#
# followed by commands that look like
#
#     ./charclass cntrl
#     ./charclass lower
#     ./charclass upper     
#     ./charclass punct     
#
# to generate the HEX code lists used in various ByteSelector recursive calls that
# implement "character class" selection. Those lists are duplicated by this script,
# but some of the entries in a few of the lists may be open to debate. At the very
# least ChatGPT, when it's asked carefully about them, seems to believe the lower,
# upper, and punct classes aren't quite right.
#
# NOTE - might need a little investigation, but not urgent right now. This script
# generates the HEX lists, exactly the way they're currently defined in existing
# bytedump bash script.
#

matchedbytes=()
bytes="TRUE"
missed=""
matched=""

while (( $# > 0 )); do
    case "$1" in
          +bytes) bytes="TRUE";;
          -bytes) bytes="";;
        +matched) matched="TRUE";;
        -matched) matched="";;
         +missed) missed="TRUE";;
         -missed) missed="";;
              --) shift; break;;
               *) break;;
    esac
    shift
done

if [[ $1 =~ ^[[:blank:]]*(alnum|alpha|blank|cntrl|digit|graph|lower|print|punct|space|upper|xdigit)[[:blank:]]*$ ]]; then
    charclass="[:${BASH_REMATCH[1]}:]"
    shift
    for range in "${@:-00-FF}"; do
        if [[ $range =~ ^(([[:xdigit:]]+)([-]([[:xdigit:]]+))?)$ ]]; then
            first="16#${BASH_REMATCH[2]}"
            last="16#${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
            for (( index = first; index <= last; index++ )); do
                state=""
                hex=$(printf "%.2X" "$index")

                #
                # Bash can't handle null bytes in strings and command substitution
                # tosses all trailing newlines, so deal with them separately.
                #
                case "${hex^^}" in
                    00) char="";;
                    0A) char=$'\n';;
                     *) char=$(printf "%b" "\u00${hex}");;
                esac

                if [[ $char =~ ^[${charclass}]$ ]]; then
                    state="${matched:+"TRUE"}"
                    matchedbytes[index]="$hex"
                elif [[ $charclass == "[:cntrl:]" && $index == "0" ]]; then
                    state="${matched:+"TRUE"}"
                    matchedbytes[index]="$hex"
                else
                    state="${missed:+"FALSE"}"
                fi

                if [[ -n $state ]]; then
                    if [[ ${char} =~ [[:print:]] ]]; then
                        printf "HEX=%s, CHAR=%s: %s\n" "$hex" "$char" "$state"
                    else
                        printf "HEX=%s, CHAR=%s: %s\n" "$hex" "^$hex" "$state"
                    fi
                fi
            done
        fi
    done
fi

if [[ -n $bytes ]]; then
    if (( ${#matchedbytes[@]} > 0 )); then
        sep=""
        for (( index = 0; index < 256; index++ )); do
            if [[ -n ${matchedbytes[$index]} ]]; then
                first="$index"
                for (( last=index; index < 256; index++ )); do
                    if [[ -n ${matchedbytes[$index]} ]]; then
                        last=$index
                    else
                        break
                    fi
                done
                printf "%s%s" "$sep" "${matchedbytes[$first]}"
                if (( last > first )); then
                    printf -- "-%s" "${matchedbytes[$last]}"
                fi
                sep=" "
            fi
        done
        printf "\n"
    fi
fi

