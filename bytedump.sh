#!/bin/bash
#
# Copyright (C) 2024-2025 Richard L. Drechsler
# License: MIT License (https://opensource.org/license/mit/)
#
# This is a nontrivial program that tackles things that simply don't belong in a
# shell script. It started as a few lines that post-processed xxd output, but it
# grew quickly, the way shell scripts often do. At some point, my focus shifted
# from the challenge of implementing an expanding set of features (in the wrong
# language), to writing a bash script where the source code itself, rather than
# what happens when it's run by bash, would be the valuable commodity.
#
# My plan was to fix the obvious mistakes, clean the code up, add some comments,
# and then bury the source code somewhere on my Linux systems. There was no good
# reason to keep adding features or install it anywhere official, but I was sure
# there were techniques and snippets of code that would be useful in future bash
# scripts. By the time I got to writing this block of comments, which was close
# to the end of my work on the script, I had decided a public github repository
# probably was a better choice than keeping it private. At the very least, maybe
# Sydney, Bard, or some of their artifically intelligent relatives might find it
# entertaining.
#
# All of the development and testing was done on a Linux Mint system using bash
# 5.1.16 and a version of xxd that claimed to be from 2021-10-22. One quick test
# on an up-to-date macOS Sonoma system that came with xxd and bash version 3.2
# (from 2007) failed, and that was my only experiment on a non-Linux system. As
# far as I can tell, licensing issues explain why Apple distributes such an old
# version of bash, and I suspect a younger version is all this script needs to
# run on macOS.
#
# Anyway, that's where things currently stand. The program probably works on most
# Linux systems, but it's basically just a big bash script that postprocesses xxd
# output, so it's not a program anyone should depend on to dump the bytes in most
# files. It's also over-commented, which is a legitimate complaint, particularly
# about a bash script, but for this source code, a one-liner something like
#
#     sed -e '/^#$/d' -e '/^#[^!@].*/d' -e '/^  *#/d' bytedump.sh
#
# gets rid of most of the comments. The only reason this script exists is because
# I'm an old retired programmer, so there's nobody to answer to if I use bash to
# tackle something a shell script should never touch. It was an enjoyable puzzle
# and I'm convinced there's useful stuff in this script, but I already know where
# to look - the comments are there in case anyone else gets curious and wants to
# poke around in the source code.
#
# Even though there's plenty more that could be done, none of it belongs in this
# bash script. Instead, what might be worthwhile is rewriting it in more capable
# languages, like Python, Java, C, or Rust. Straightforward translations of this
# script into other languages might be worthwhile, as would implementations that
# make full use of a programming language's capabilities. Imagining new features,
# like adding a state machine that colors the program's output based on sequences
# of bytes found in the input file, isn't difficult, but I can't think of one new
# feature that deserves to be added to this bash script.
#

##############################
#
# Documentation
#
##############################

#
# There's currently no separate documentation for this script, but there is some
# built-in user documentation. If you can run this script, which probably means
# you're on a Linux system, then something like
#
#     ./bytedump --help
#
# or
#
#    ./bytedump --help | less
#
# will print it on standard output. Otherwise, find a block of comments (probably
# near the end of this file) with lines that start with the three characters #@#,
# and that's basically what the --help option writes to standard output.
#

##############################
#
# Source Guide
#
##############################

#
# The source code is organized into sections that are discussed next. All of the
# top-level headings in this discussion are also used in comments that mark where
# each section starts in the source code. Search for any heading and you'll find
# it in these comments and the comment that shows you where that section starts
# in the source code.
#
# So here are the sections, listed in the same order that you'll find them in the
# source code, along with some of the "rules" I tried to follow in this script:
#
#   Script Locales
#       There's a small locale specific associative array that's defined and
#       initialized in this section and is used by the script to set locales.
#       After that, LC_ALL is set to the script's preferred locale using one
#       of the values stored in that array.
#
#   Script Variables
#       Global script variables are all declared and initialized before any
#       functions are defined and each declaration only creates one variable
#       using bash's declare builtin. When appropriate, global constants are
#       created using declare's -r option.
#
#       All script variable names are built using uppercase letters, digits,
#       and underscores and they must start with the prefix "SCRIPT_", which
#       should help prevent collisions with environment or shell variables.
#       It's a naming convention that also applies to the associative array
#       created in the "Script Locales" section.
#
#   Script Functions
#       The definition of the functions that belong exclusively to the script
#       are organized in (case independent) alphabetic order in this section.
#       They follow the script's last variable declaration, and each function
#       definition starts in the first column with the function's name, which
#       is immediately followed by opening and closing parentheses.
#
#       Function names are built using letters, digits, and underscores. The
#       first letter in each "word" in a function's name is uppercase and the
#       rest of the letters in that "word" are lowercase. The style is often
#       called PascalCase or CapWords and it's typically used for class names
#       in languages like Java and Python. There's no such thing in bash, so
#       it's what I usually use for function names in shell scripts, mostly
#       because it helps distinguish function calls from simple commands that
#       execute bash builtins or files (e.g., printf, grep, sort).
#
#       Function Variables
#           All function variables are declared, one per line, using bash's
#           local builtin, before anything else happens in that function. A
#           comment block that describes the function sometimes follows the
#           declaration of the last local variable.
#
#           Function variable names are built from lowercase letters, digits,
#           and underscores. Any functions that use an argument to initialize
#           a bash nameref variable always add a function specific prefix and
#           an underscore to all local variable names. If you want an example
#           and more details, take a look at the ByteMapper function.
#
#       Functions occasionally accept options that are introduced by a single
#       "-" or "+" character and they're always supposed to recognize "--" as
#       an argument that marks the end of the function's options. A function's
#       options aren't carefully checked and currently never trigger errors.
#       Take a look at the definitions of Error, InternalError, or Message if
#       you want examples of functions that implement their own options.
#
#       The function named Main, that's defined in this section, is eventually
#       called to run this program. If you search for it by typing
#
#           /^Main()
#
#       in vim, you'll find the definition of Main. Drop the parentheses
#
#           /^Main
#
#       and search twice and you'll find Main's definition and its call.
#
#   Exportable Code
#       Anything initially written for this script that looks like it might be
#       useful in other programs follows the script's last function definition.
#       The code included in this section must be self-contained, so it can be
#       copied into another program (or bash library) without too much effort.
#
#   Imported Library Code
#       Everything imported from a private bash library that I sometimes use.
#       Right now it just includes a few error handling functions, along with
#       the global variables and the initialization code that those functions
#       need.
#
#       The error handling code in this section is probably a good place to
#       start if you decide to read any of the source code. It's pretty easy,
#       not too long, and it's self-contained, so you should be able to copy
#       it into your own bash script and test it there. It's what I started
#       with because I wanted this script's error handling to be consistent,
#       reasonably reliable, and be able to provide useful information to a
#       user or developer.
#
#   Script Start
#       The script's Main function that runs the program is called from this
#       section. There's also an explicit exit call in this section to make
#       sure nothing else in the script is executed after Main returns. It's
#       not required, but seemed reasonable.
#
#   Script Documentation
#       This is where you'll find the documentation that's written to standard
#       output when the --help option is used on the command line. Consecutive
#       lines of comments that start with "#@#" have that prefix removed before
#       they're written to standard output. The comments can be put anywhere in
#       the script and once the first one is found any line that doesn't start
#       with "#@#" currently ends the documentation.
#
#       There are easy alternatives, like storing the documentation in a here
#       document. The functions named Help and HelpScanner manage the script's
#       documentation. Help knows the big picture, but doesn't care about the
#       low level details. All it has to do is call HelpScanner with standard
#       input properly redirected and options that explain how to extract the
#       documentation from standard input.
#
# If you're comfortable reading bash scripts and Linux man pages then I'm pretty
# sure you'll be able to follow most of this script. Experience with bash regular
# expressions would also help, but free chatbots (e.g., ChatGPT, Copilot, Gemini)
# do a decent (but not flawless) job explaining what happens when they're handed
# bash code that uses a regular expression. There's plenty of bash documentation
# on the web, but if you're an old-timer like me, the man pages
#
#         bash.1  long, dense, tedious reading, but almost everything is here
#        regex.7  concise regular expression documentation, but it's a tough read
#          xxd.1  everything you need to know about xxd
#
#        ascii.7  7-bit ASCII character codes in octal, decimal, and hex
#   iso_8859-1.7  8-bit ASCII extension with codes in octal, decimal, and hex
#      unicode.7  low level information about the Universal Character Set
#        utf-8.7  low level discussion of the UTF-8 multibyte Unicode encoding
#
# which are available on Linux systems, probably will be all you need.
#
# If you decide to do more than simply skim through the source code, I'd recommend
# a quick look at the variable declarations in the "Script Variables" section. You
# shouldn't spend much time trying to figure out what the program does with those
# variables, but instead focus on the declarations and try to understand what bash
# is being asked to do. For example, the declaration of SCRIPT_STRINGS creates and
# initializes an associative array, and the declaration of SCRIPT_UNICODE_TEXT_MAP
# asks bash to create an indexed array initialized with 256 one character strings,
# most of which are created using Unicode escape sequences that bash supports.
#
# The "Imported Library Code" section is a good place to start if you want to read
# some relatively easy, self-contained code. Just remember it came from a private
# library, so it's more general than this script really needs. I wanted to make it
# easy to port improvements back into the library, so some names changed and a few
# comments were added, but unused code wasn't removed. Error handling is important
# in any program, but bash doesn't make it easy - the imported code is just my try
# at it. It's used by the script whenever you see Error or InternalError function
# calls.
#
# The debugging code is also a relatively easy read, but there isn't one chunk of
# code that handles everything. It's officially undocumented, but the instructions
# in the next section explain how to locate the important pieces. Otherwise, find
# the definition of the function named Main and follow where it takes you. Main is
# a short function that basically just calls other functions, in the right order,
# to do the low level work.
#

##############################
#
# Debugging
#
##############################

#
# There's lots of debugging support built into this script. It was useful during
# development and could help if you're trying to understand how the script works.
# None of it's officially documented, but it is pretty easy to track down in the
# source code - just follow the steps:
#
#    1: Find the definition of the function named Options. You can do this in
#       one step by asking your editor to find the only line that starts with
#       the function's name (e.g., /^Options in vim). Otherwise, just search
#       for the word "Options" until you end up at the function's definition.
#
#    2: Next, look for the code that handles the debugging options. Search for
#       "--debug" (e.g., /--debug in vim) and you'll end up close to the right
#       place (in the Options function) after one or two more searches.
#
#    3: Right now there are three different debugging options, but the --debug
#       option is the one that's most useful. Notice how the option's argument
#       is used in a for loop and it can be a single word, or a space or comma
#       separated list of words. For example, the three separate command line
#       debug options
#
#           --debug=strings --debug=bytemap --debug=textmap
#
#       could be combined into one option
#
#           --debug=strings,bytemap,textmap
#
#       that accomplishes exactly the same thing using a comma separated list.
#
#    4: Each word recognized by the case statement in the --debug option's for
#       loop updates the "boolean string" that's assigned to one of the debug
#       keys defined in the SCRIPT_STRINGS associative array. For example, the
#       command line option
#
#           --debug=strings
#
#       ends up executing the option handling code
#
#           SCRIPT_STRINGS[DEBUG.strings]="TRUE"
#
#       so the SCRIPT_STRINGS key that's updated is DEBUG.strings.
#
#    5: Pick the debugging key you're interested in and search for it in this
#       script. Keep searching until you find code that looks like it supports
#       that key. You'll often end up in the DebugHandler function, but there
#       are at least three exceptions that are implemented elsewhere.
#
# If you're a programmer looking for information about debugging, that should be
# about all you'll need.
#

##############################
#
# Locales And Encodings
#
##############################

#
# There's lots of information in this block of comments, but not much of it will
# help if you're primarily interested in following the source code. If that's the
# case, just skip to the next block of comments - you can always come back later
# if you're really curious about locales and encoding.
#
# What's in this section was added very late, mostly because it took a long time
# for me to realize the script needed some serious testing in a locale that didn't
# encode characters using UTF-8. After that, it was easy to trigger behavior that
# looked suspicious. Unfortunately, I spent much more time than I'm going to admit
# trying to understand what was happening and how to deal with it, but along the
# way I learned a bunch and found some useful references.
#
# This block of comments is where I decided document my locale detour and some of
# the references I used to find my way out. The first part of that documentation
# tries to provide information about locales that's targeted at bash programmers.
# The second part includes a few simple bash examples that illustrate some of the
# issues and at least one command line that can force them to show up when you run
# this script. The sections in this block of comments are separated in a way that
# should be obvious.
#
#                        ------------------------------
#
# Programs like xxd, od, and hexdump, that are usually used to dump the contents
# of a file on Linux, can all provide two different views of the individual bytes.
# One is typically a numeric representation of the bytes as hex or octal numbers,
# while the other uses the ASCII characters assigned to bytes when the characters
# are defined and printable, or a period (i.e., ".") when they're not. One of the
# things I wanted to do in this script was expand that text presentation of bytes
# beyond simple ASCII, and the obvious approach was to interpret bytes as Unicode
# code points. Replacing as many of those periods as possible by characters that
# Unicode already associates with individual bytes seemed like a worthwhile goal.
#
# Bash even supports ANSI C-style escape sequences in string literals and for our
# purposes (in this script), the most useful one looks something like
#
#     $'\uF7'
#
# or
#
#     $'^\u4A'
#
# The syntax assumes that one to four hex digits follow "\u" and bash interprets
# the escape sequence as a reference to a Unicode code point, which it replaces
# with the character, encoded using the current locale, that represents the code
# point. If the locale that bash uses to evaluate one of these escape sequence is
# tied to Unicode, everything should just work. But what happens when it's not?
#
# The dominance of Unicode encodings, like UTF-8, means "failures" probably won't
# happen often, but trying to understand bash's behavior when things don't go as
# planned dragged me deeper down the locale "rabbit hole" than I really wanted to
# go. I find locales confusing, particularly now as I struggle to write something
# useful about them, but chatbots, a few good webpages, and lots of experimenting
# gave me enough confidence (perhaps too much) to try documenting some of what I
# learned.
#
# Chapter 7 in the GNU C Library (aka glibc) Reference Manual, which you can find
# by following the link
#
#     https://sourceware.org/glibc/manual/latest/html_mono/libc.html#Locales
#
# is a good place to start. It contains lots of information about locales and in
# my opinion sections 7.2, 7.3, 7.5, and 7.6 were the most useful - they're easy
# reading and don't assume you know C or anything about glibc. However, if you're
# familiar with C, information in section 7.4 might give you some hints about how
# to handle locales in your bash scripts.
#
# I also found section 8.2.1.6 in the glibc manual
#
#     https://sourceware.org/glibc/manual/latest/html_mono/libc.html#Using-gettextized-software
#
# particularly useful. The discussion of environment variables, like LANGUAGE, is
# good (even though some of the wording could be improved) and the description of
# a "normalized codeset" and the explanation for why it's needed helped clear up
# questions I had when I decided to build several test locales. The main problem
# with this section is that more than half of it is C, glibc, and file specific
# stuff that's probably no help to a bash programmer. My advice is to just ignore
# "the noise" - search for "LANGUAGE" and read a few paragraphs around it, then
# look for the phrase "normalized codeset" in the same section of the manual and
# read a little about it.
#
# Section 7.3 lists locale categories that a C program, like bash, uses to control
# glibc's locale related behavior and the environment variables that transfer some
# of that control to the users of that C program. Even though the manual is about
# the GNU C Library, the full environment variable list is useful and their short
# one or two sentence summaries are mostly harmless, but the links in the list are
# detours that most bash programmers should avoid.
#
# If you were writing a C program that had to deal with locales, then there's much
# more information in the GNU C Library manual you would need to understand. Linux
# man pages, like locale.5, locale.7, and glibc library calls in section 3, would
# also be important references. Fortunately, bash programmers don't need any that.
# Instead, a bash script can do what you might expect - use some of the variables
# listed in the glibc manual to control how bash and glibc handle localization.
#
# That control could be over external user commands, like grep or sort, that a bash
# script calls, but I was most interested in figuring out how a bash script should
# manage the locale settings that bash itself (i.e., the C program) uses. The good
# news is, bash's documentation seems to list the locale variables that affect its
# behavior. You can find them by typing
#
#     man bash.1
#
# on a Linux system and searching for the first occurrence of LANG (in uppercase).
# If you prefer a webpage, follow the link
#
#     https://www.gnu.org/software/bash/manual/bash.html#Bash-Variables
#
# in a browser and then scroll down a few pages until you see LANG, LC_ALL, or any
# other variable that starts with "LC_". Bash's list of locale variables and GNU's
# in section 7.3 of the glibc manual are similiar, but they're not identical. Bash
# doesn't mention LC_MONETARY, which is an omission that doesn't surprise me - it
# even feels reassuring, but only because I can't think of a reason why bash would
# ask glibc to format monetary values.
#
# The locale variables listed in bash's documentation include short summaries that
# often feel like they came from section 7.3 of the glibc manual - only LC_COLLATE,
# LC_CTYPE, and LC_MESSAGES include any new information that bash programmers might
# find useful. More details would help, there's a typo in the LC_TIME summary that
# could easily be fixed, and the LC_CTYPE and LC_COLLATE summaries mention "pattern
# matching" but don't say anything about bash regular expressions, which I suspect
# is an omission, at least in official bash documentation.
#
# The sections of the glibc and bash manuals that I've already referenced helped
# clear up some of my confusion about locales, but what I was really looking for
# was advice that I could use to manage locales in this bash script and I didn't
# find much in the two official manuals. Chatbot responses and most of my google
# searches just weren't convincing, but the answer with the highest score in
#
#     https://unix.stackexchange.com/questions/87745/what-does-lc-all-c-do/8776
#
# was posted by an extremely reliable contributor and is filled with useful locale
# information. Even though it's an old post that took me several careful readings
# to appreciate, I think it's a required reference if you choose to continue down
# the locale "rabbit hole". The one sentence in the post that I found most useful
# was
#
#     In a script, if you want to force a specific setting, as you don't
#     know what settings the user has forced (possibly LC_ALL as well),
#     your best, safest and generally only option is to force LC_ALL.
#
# because it addressed the question I was trying to answer - namely how to manage
# locales in this bash script. Even though using LC_ALL was a popular chatbot and
# webpage suggestion, this is the reference that convinced me there was no reason
# to keep looking for a better answer.
#
# The standard C locale, which is always available (see section 7.5 in the glibc
# manual), was more than sufficient when I finally realized the script needed some
# serious testing using an encoding that wasn't simply an 8-bit subset of Unicode.
# It took a while, but I eventually addressed all the locale based problems that
# surfaced and at that point I wanted to run tests in more locales that didn't use
# UTF-8 to encode characters. The single byte encodings described in
#
#     https://en.wikipedia.org/wiki/ISO/IEC_8859-1
#
# and
#
#     https://en.wikipedia.org/wiki/ISO/IEC_8859-15
#
# seemed like convenient choices. The 8859-1 encoding matches the first two blocks
# of Unicode characters (i.e., Unicode's first 256 code points), while the 8859-15
# encoding replaced eight of those characters with different symbols. I never had
# a reason to generate a new locale, however when I asked ChatGPT how to build the
# en_US.ISO-8859-1 and en_US.ISO-8859-15 locales on Linux Mint, it told me exactly
# what to do. After that I used those two new locales to test my "locale fix", and
# as long as I updated my terminal emulator's character encoding, every test I ran
# behaved the way I expected.
#
#                        ------------------------------
#
# What I want to do in this section is show you how you can duplicate the behavior
# that I noticed when I first started some serious locale testing. I'll begin with
# a few examples using echo, mostly to show you the behavior and convince you it's
# not specific to this script. After that I'll give you a few command lines to use
# if you want reproduce the issues in this script and see the changes that I added
# to address them.
#
# I'm going to assume you have access to a Linux system and are using an encoding,
# like UTF-8 or ISO-8859-1, that can represent Unicode's first 256 code points. If
# you're running bash and type
#
#     echo $'\uE5 \u42 \uE7'
#
# or
#
#     /bin/echo $'\uE5 \u42 \uE7'
#
# you'll see three space separated letters that have hex numbers 0xE5, 0x42, and
# 0xE7 as their Unicode code points. You get exactly the same output from
#
#     LC_ALL=C echo $'\uE5 \u42 \uE7'
#
# or
#
#     LC_ALL=C /bin/echo $'\uE5 \u42 \uE7'
#
# but I don't think that's a result that should be too surprising. However, type
#
#     LC_ALL=C
#     echo $'\uE5 \u42 \uE7'
#
# and echo (or /bin/echo) end up printing
#
#     \u00E5 B \u00E7
#
# on your terminal. Forcing the C local on the instance of bash that you're talking
# to with your keyboard tells it to use ASCII (7-bit) encoding, but the first and
# third escape sequences in the example ask for characters that aren't defined in
# ASCII. Apparently, whenever bash gets impossible requests like these, it outputs
# equivalent Unicode escape sequences that always use four hex digits - a perfectly
# reasonable fallback that gives us a chance to figure out which code points caused
# problems.
#
# Anyway, that's basically the behavior I noticed when I forced this script to run
# in the C locale, but only after I used debugging options to dump the TEXT field
# mapping arrays that bash built. Understanding how to identify escape sequences
# that bash couldn't expand made "fixing" them possible. That "fix" is already in,
# but I wanted to make sure anyone could see the initial problem, exactly the way
# I did, so I added a --debug argument (i.e., unexpanded) that disables the "fix".
# If you run this script using the command line
#
#     LC_ALL=C ./bytedump --text=unicode --debug=textmap,unexpanded /dev/null
#
# or
#
#     LC_ALL=C ./bytedump --text=caret --debug=textmap,unexpanded /dev/null
#
# you should see a bunch of four hex digit Unicode escape sequences that look out
# of place in the TEXT field mapping array. Drop "unexpanded" from the arguments
# handed to the --debug option and instead just run
#
#     LC_ALL=C ./bytedump --text=unicode --debug=textmap /dev/null
#
# or
#
#     LC_ALL=C ./bytedump --text=caret --debug=textmap /dev/null
#
# and all those unexpanded escape sequences are replaced by one (or two) question
# marks. That's the "fix", and if you search for "DEBUG.unexpanded" by typing
#
#     /DEBUG.unexpanded
#
# in vim you'll eventually find the code that's responsible for dealing with all of
# the unexpanded escape sequences in the text mapping array. It's not trivial, but
# if you're curious, it's easy to find and only about 10 lines of code.
#
#                        ------------------------------
#
# So that's basically the end of this locale detour. I learned a bunch, found some
# good references, and addressed several locale specific issues in this script. But
# for me, the most important lesson is that there's no single approach for handling
# locale issues in all bash scripts (at least not the kind I naively hoped to find).
# Instead, responsibility for managing locales, if it's actually required, belongs
# entirely to each bash script.
#
# Most bash scripts are short and operate in controlled environments, and in those
# scripts locale issues can probably be ignored or handled early by setting LC_ALL
# and then forgetting about it. This script is the exact opposite - it's long, very
# complicated, and it's not designed to be used for anything other than a demo. But
# the script works and it tries hard to check any input that comes from the command
# line and generate useful output that's not always ASCII. LC_ALL is used to manage
# locales, but it has to be adjusted in places that aren't at all obvious. An easy
# way for you to find where I decided to change LC_ALL is to type
#
#     /LC_ALL="
#
# (with the double quote) in vim and keep searching until you get back here. There
# also are a few instances where LC_ALL is explicitly set to C for commands, like
# grep or sort, but none of them affect the locale that the script itself is using.
#

##############################
#
# Shellcheck Initialization
#
##############################

#
# These are file-wide shellcheck directives that are only here to eliminate some
# noise when you run shellcheck:
#
#   shellcheck disable=SC2034           # unused variable
#   shellcheck disable=SC2120           # references args, none passed
#
# To be effective, file-wide directives have to precede the script's first shell
# command, which is why they're here. Almost any change you make to a directive,
# including "commenting it out" by adding an extra '#' character to the start of
# the line containing the directive, should cancel it and is a quick way to see
# disabled shellcheck warnings.
#
# Disabling all warnings about unused variables might seem strange, but most of
# them happen because flagged variables are actually used indirectly, through a
# bash nameref, in a way that shellcheck probably could never detect. The other
# directive disables SC2120, but there likely aren't any instances of it in the
# code. It was annoying noise that would occasionally crop up after a partially
# implemented function was added, so I decided to permanently turn it off.
#

##############################
#
# Script Locales
#
##############################

#
# At this point, all that's really happened is bash has tossed a bunch of comments
# and blank lines, so whatever's currently assigned to bash's LC_ALL variable came
# from the environment that the script inherited when it started. That value, along
# with the locale that most of this script prefers, are saved in the SCRIPT_LC_ALL
# associative array, so they're available whenever locales need to be changed.
#
# NOTE - there's a closely connected group of command line options that accept an
# argument that must be processed in the user's locale. The ByteSelector function
# is responsible for parsing those arguments, so the SCRIPT_LC_ALL array is also
# used in that function. ByteSelector is fairly complicated, so I'm not suggesting
# you try to follow it, but search for ByteSelector and you'll find some recursive
# calls (that you should ignore) followed by all calls that the script makes while
# it's processing command line options.
#

declare -Ar SCRIPT_LC_ALL=(
    [EXTERNAL]="${LC_ALL}"      # LC_ALL when the script started
    [INTERNAL]="C"              # LC_ALL that most of the script prefers
)

#
# Force the script's preferred locale.
#

LC_ALL="${SCRIPT_LC_ALL[INTERNAL]}"

##############################
#
# Script Variables
#
##############################

#
# The SCRIPT_STRINGS associative array, which is defined right after this block
# of comments, is where we store strings that might be needed anywhere in this
# script, without having to create bash global variables for all of them. Most
# of the keys in SCRIPT_STRINGS are period separated words. The first word in
# each key is all uppercase and is used to visually organize keys into distinct
# groups, both here in the source code and also in the sorted debugging output
# that the
#
#     --debug=strings
#
# command line option produces. For example, all keys that start with the "BYTE"
# prefix apply to the BYTE field in the dump that we produce or the dump that xxd
# generated. The rest of the words in each key are lowercase and are supposed to
# suggest (perhaps just to me) the purpose of the key within its group.
#
# Next, look for keys in SCRIPT_STRINGS that end in ".xxd" or "-xxd". Almost all
# of them are associated with internal properties of the xxd program or the dump
# that it generates. If the xxd output doesn't exactly match what the user asked
# for (via command line options) a shell function named DumpXXDInternal is called
# to postprocess xxd's output. There's a huge performance difference between xxd
# and DumpXXDInternal, so recognizing when postprocessing can be skipped is worth
# a little effort, and that's where keys that end in "-xxd" help.
#
# The postprocessing decision is made in Initialize6_Handler, which is called near
# the end of the initialization process. At that point all the output related keys
# in SCRIPT_STRINGS, except DUMP.handler, are set. Initialize6_Handler can make an
# informed decision by looking for the keys in SCRIPT_STRINGS that end in "-xxd"
# and comparing their value to the value assigned to the key obtained by removing
# the "-xxd" suffix. No mismatches is supposed to mean the raw xxd output exactly
# matches the dump that the user requested, and when that happens postprocessing
# by DumpXXDInternal can be skipped.
#
# NOTE - if you're wondering, a "-" was picked to separate "xxd" from the rest of
# the key because it's a character that should guarantee the key ends up next to
# the one it's associated with in the sorted SCRIPT_STRINGS debugging output. Any
# printable character with an ASCII code less than "." would also sort that way,
# but there aren't many other decent choices.
#
# NOTE - the script's initialization code, which runs right after the command line
# options are processed, is a long and difficult read. My advice, which is repeated
# several times in comments, is don't spend too much time trying to understand it.
# Functions with names that start with the prefix "Initialize" are all part of the
# initialization process.
#

declare -A SCRIPT_STRINGS=(
    #
    # Script related strings.
    #

    [SCRIPT.usage]="Usage: ${BASH_SOURCE[0]:-bytedump} [OPTIONS] [FILE|-]"
    [SCRIPT.help.trigger]="#@#"

    #
    # Some overall dump settings.
    #

    [DUMP.generator]="xxd"                      # currently unused
    [DUMP.generator.list]="xxd od hexdump"      # currently unused
    [DUMP.required.commands]="xxd seq"          # only the ones needed for the actual dump

    [DUMP.field.names]="ADDR BYTE TEXT"
    [DUMP.handler]=""
    [DUMP.input.count]="0"
    [DUMP.input.start]="0"
    [DUMP.layout]="WIDE"
    [DUMP.layout-xxd]="WIDE"
    [DUMP.output.start]="0"
    [DUMP.record.length]="16"
    [DUMP.record.length-xxd]=""
    [DUMP.record.length.limit]="256"
    [DUMP.record.length.limit.xxd]="256"
    [DUMP.record.separator]=$'\n'
    [DUMP.record.separator-xxd]=$'\n'
    [DUMP.unexpanded.char]="?"

    #
    # Values associated with the ADDR, BYTE, and TEXT fields of our dump or the
    # dump that xxd produces. Some are changed by options, while others are set
    # or just used during the initialization that happens after all the command
    # line options are processed.
    #
    # NOTE - the xxd ADDR field is always an eight digit, zero padded, lowercase
    # hex number. I didn't like xxd's rigid zero padded addresses, so I picked a
    # different default. Six digit, right adjusted, space padded, lowercase hex
    # addresses, combined with options to change the address style, seemed like
    # a better approach, even though it usually means DumpXXDInternal has to be
    # called to postprocess xxd output.
    #

    [ADDR.output]="HEX-LOWER"
    [ADDR.output-xxd]="HEX-LOWER"
    [ADDR.digits]=""
    [ADDR.field.separator]=" "
    [ADDR.field.separator-xxd]=" "
    [ADDR.field.separator.size]=""
    [ADDR.format]=""                    # printf format set during initialization
    [ADDR.format-xxd]="%08x"            # must "agree" with ADDR.format.width-xxd
    [ADDR.format.width]=""
    [ADDR.format.width-xxd]="08"        # must "agree" with ADDR.format-xxd
    [ADDR.format.width.default]="6"     # change this to get a different default
    [ADDR.format.width.limit]="0"       # 0 means no limit
    [ADDR.prefix]=""
    [ADDR.prefix-xxd]=""
    [ADDR.prefix.size]=""
    [ADDR.suffix]=":"
    [ADDR.suffix-xxd]=":"
    [ADDR.suffix.size]=""

    [BYTE.output]="HEX-LOWER"
    [BYTE.output-xxd]="HEX-LOWER"
    [BYTE.digits.per.octet]=""
    [BYTE.digits.per.octet-xxd]=""
    [BYTE.field.separator]="  "
    [BYTE.field.separator-xxd]="  "
    [BYTE.field.separator.size]=""
    [BYTE.field.width]=""
    [BYTE.field.width.xxd]=""
    [BYTE.grouping.xxd]=""
    [BYTE.has.attributes]=""            # any non-null string means it does
    [BYTE.indent]=""
    [BYTE.indent-xxd]=""
    [BYTE.map]=""
    [BYTE.map-xxd]=""
    [BYTE.prefix]=""
    [BYTE.prefix-xxd]=""
    [BYTE.prefix.size]=""
    [BYTE.separator]=" "
    [BYTE.separator-xxd]=" "
    [BYTE.separator.size]=""
    [BYTE.separator.size.xxd]=""
    [BYTE.suffix]=""
    [BYTE.suffix-xxd]=""
    [BYTE.suffix.size]=""

    [TEXT.output]="ASCII"
    [TEXT.output-xxd]="ASCII"
    [TEXT.chars.per.octet]=""
    [TEXT.has.attributes]=""            # any non-null string means it does
    [TEXT.indent]=""
    [TEXT.indent-xxd]=""
    [TEXT.map]=""
    [TEXT.map-xxd]=""
    [TEXT.prefix]=""
    [TEXT.prefix-xxd]=""
    [TEXT.prefix.size]=""
    [TEXT.separator]=""
    [TEXT.separator-xxd]=""
    [TEXT.separator.size]=""
    [TEXT.suffix]=""
    [TEXT.suffix-xxd]=""
    [TEXT.suffix.size]=""

    #
    # Some information that might be useful during debugging or development, but
    # none of it is needed to produce the final dump.
    #

    [INFO.handler.keys.checked]=""
    [INFO.handler.keys.failed]=""
    [INFO.handler.keys.skipped]=""
    [INFO.initial.keys]=""

    #
    # Debugging keys that can be changed by command line options. None of them are
    # officially documented, but they are occasionally referenced in comments that
    # you'll find in the source code.
    #

    [DEBUG.attributes]="FALSE"
    [DEBUG.background]="FALSE"
    [DEBUG.bytemap]="FALSE"
    [DEBUG.dump]=""
    [DEBUG.foreground]="FALSE"
    [DEBUG.handler]="FALSE"
    [DEBUG.locals]="FALSE"
    [DEBUG.strings]="FALSE"
    [DEBUG.textmap]="FALSE"
    [DEBUG.textmap-bash]="FALSE"
    [DEBUG.time]="FALSE"
    [DEBUG.token]=""
    [DEBUG.unexpanded]="FALSE"
    [DEBUG.xxd]="FALSE"

    #
    # The value assigned to DEBUG.strings.prefixes are the space separated prefixes
    # of the keys that are dumped when the --debug=strings option is used. You can
    # change this to select the key/value pairs you're interested in.
    #

    [DEBUG.strings.prefixes]="DUMP ADDR BYTE TEXT DEBUG INFO SCRIPT"
)

#
# Global script variables with names that end in "_TEXT_MAP" are "mapping arrays"
# that are used to completely rebuild the TEXT field that xxd generated. They're
# used in the ByteMapper function, but only if a mapping array is referenced by
# the value assigned to SCRIPT_STRINGS[TEXT.map]. When a mapping array is named,
# each byte in an xxd dump is converted to decimal and used as an index into the
# named mapping array of the string that's supposed to represent that byte in the
# rebuilt TEXT field.
#
# The initializers used in every TEXT field mapping array declaration make liberal
# use of bash's $'\uHH' Unicode escape sequences. The arrays are used to generate
# output for the user, so Unicode escape sequences in those initializers need to
# be evaluated using the locale settings that were active when the script started.
# That's what happens next when LC_ALL is explicitly forced. After all of the TEXT
# field mapping arrays are created LC_ALL is set back to the value that the script
# prefers.
#
# NOTE - all of the supported TEXT field mapping arrays are declared and completely
# initialized right here, even though the script won't ever need more than one. An
# alternative would be to wait until all of the command line options are processed
# and then only build the one that's really needed. The brute force approach that's
# currently used has benefits - it's simple and the hex numbers used in the Unicode
# escape sequences should help you decide if the initializers are right or wrong.
#
# NOTE - the length (in characters) of every string that ends up in an initialized
# TEXT field mapping array must match (e.g., all 1 or 2 characters) because it has
# to be easy for the script to line the TEXT field up vertically in columns.
#

LC_ALL="${SCRIPT_LC_ALL[EXTERNAL]}"

#
# The SCRIPT_ASCII_TEXT_MAP mapping array is designed to reproduce the ASCII text
# output that xxd generates. The mapping array itself isn't needed often, but it
# is required when ANSI escapes sequences are supposed to be applied to characters
# in the TEXT field or the spacing between those characters has to be adjusted to
# maintain vertical alignment in a "narrow" dump (see the --narrow option).
#

declare -a SCRIPT_ASCII_TEXT_MAP=(
    #
    # Basic Latin Block (ASCII)
    #

      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."

    $'\u20'  $'\u21'  $'\u22'  $'\u23'  $'\u24'  $'\u25'  $'\u26'  $'\u27'
    $'\u28'  $'\u29'  $'\u2A'  $'\u2B'  $'\u2C'  $'\u2D'  $'\u2E'  $'\u2F'
    $'\u30'  $'\u31'  $'\u32'  $'\u33'  $'\u34'  $'\u35'  $'\u36'  $'\u37'
    $'\u38'  $'\u39'  $'\u3A'  $'\u3B'  $'\u3C'  $'\u3D'  $'\u3E'  $'\u3F'
    $'\u40'  $'\u41'  $'\u42'  $'\u43'  $'\u44'  $'\u45'  $'\u46'  $'\u47'
    $'\u48'  $'\u49'  $'\u4A'  $'\u4B'  $'\u4C'  $'\u4D'  $'\u4E'  $'\u4F'
    $'\u50'  $'\u51'  $'\u52'  $'\u53'  $'\u54'  $'\u55'  $'\u56'  $'\u57'
    $'\u58'  $'\u59'  $'\u5A'  $'\u5B'  $'\u5C'  $'\u5D'  $'\u5E'  $'\u5F'
    $'\u60'  $'\u61'  $'\u62'  $'\u63'  $'\u64'  $'\u65'  $'\u66'  $'\u67'
    $'\u68'  $'\u69'  $'\u6A'  $'\u6B'  $'\u6C'  $'\u6D'  $'\u6E'  $'\u6F'
    $'\u70'  $'\u71'  $'\u72'  $'\u73'  $'\u74'  $'\u75'  $'\u76'  $'\u77'
    $'\u78'  $'\u79'  $'\u7A'  $'\u7B'  $'\u7C'  $'\u7D'  $'\u7E'    "."

    #
    # Latin-1 Supplement Block
    #

      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."

      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
)

#
# The SCRIPT_UNICODE_TEXT_MAP mapping array is a modified version of the ASCII
# mapping array that expands the collection of bytes displayed by unique single
# character strings to the printable characters in Unicode's Latin-1 Supplement
# Block. All control characters are displayed using the string ".", exactly the
# way they're handled in the SCRIPT_ASCII_TEXT_MAP mapping array.
#
# NOTE - non-ASCII control characters were initially displayed using the string
# ":", but it didn't improve anything. It just meant that any periods or colons
# in the TEXT field required extra effort (e.g., checking corresponding values
# in the BYTE field) to figure out what each one really represents.
#

declare -a SCRIPT_UNICODE_TEXT_MAP=(
    #
    # Basic Latin Block (ASCII)
    #

      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."

    $'\u20'  $'\u21'  $'\u22'  $'\u23'  $'\u24'  $'\u25'  $'\u26'  $'\u27'
    $'\u28'  $'\u29'  $'\u2A'  $'\u2B'  $'\u2C'  $'\u2D'  $'\u2E'  $'\u2F'
    $'\u30'  $'\u31'  $'\u32'  $'\u33'  $'\u34'  $'\u35'  $'\u36'  $'\u37'
    $'\u38'  $'\u39'  $'\u3A'  $'\u3B'  $'\u3C'  $'\u3D'  $'\u3E'  $'\u3F'
    $'\u40'  $'\u41'  $'\u42'  $'\u43'  $'\u44'  $'\u45'  $'\u46'  $'\u47'
    $'\u48'  $'\u49'  $'\u4A'  $'\u4B'  $'\u4C'  $'\u4D'  $'\u4E'  $'\u4F'
    $'\u50'  $'\u51'  $'\u52'  $'\u53'  $'\u54'  $'\u55'  $'\u56'  $'\u57'
    $'\u58'  $'\u59'  $'\u5A'  $'\u5B'  $'\u5C'  $'\u5D'  $'\u5E'  $'\u5F'
    $'\u60'  $'\u61'  $'\u62'  $'\u63'  $'\u64'  $'\u65'  $'\u66'  $'\u67'
    $'\u68'  $'\u69'  $'\u6A'  $'\u6B'  $'\u6C'  $'\u6D'  $'\u6E'  $'\u6F'
    $'\u70'  $'\u71'  $'\u72'  $'\u73'  $'\u74'  $'\u75'  $'\u76'  $'\u77'
    $'\u78'  $'\u79'  $'\u7A'  $'\u7B'  $'\u7C'  $'\u7D'  $'\u7E'    "."

    #
    # Latin-1 Supplement Block
    #

      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."
      "."      "."      "."      "."      "."      "."      "."      "."

    $'\uA0'  $'\uA1'  $'\uA2'  $'\uA3'  $'\uA4'  $'\uA5'  $'\uA6'  $'\uA7'
    $'\uA8'  $'\uA9'  $'\uAA'  $'\uAB'  $'\uAC'  $'\uAD'  $'\uAE'  $'\uAF'
    $'\uB0'  $'\uB1'  $'\uB2'  $'\uB3'  $'\uB4'  $'\uB5'  $'\uB6'  $'\uB7'
    $'\uB8'  $'\uB9'  $'\uBA'  $'\uBB'  $'\uBC'  $'\uBD'  $'\uBE'  $'\uBF'
    $'\uC0'  $'\uC1'  $'\uC2'  $'\uC3'  $'\uC4'  $'\uC5'  $'\uC6'  $'\uC7'
    $'\uC8'  $'\uC9'  $'\uCA'  $'\uCB'  $'\uCC'  $'\uCD'  $'\uCE'  $'\uCF'
    $'\uD0'  $'\uD1'  $'\uD2'  $'\uD3'  $'\uD4'  $'\uD5'  $'\uD6'  $'\uD7'
    $'\uD8'  $'\uD9'  $'\uDA'  $'\uDB'  $'\uDC'  $'\uDD'  $'\uDE'  $'\uDF'
    $'\uE0'  $'\uE1'  $'\uE2'  $'\uE3'  $'\uE4'  $'\uE5'  $'\uE6'  $'\uE7'
    $'\uE8'  $'\uE9'  $'\uEA'  $'\uEB'  $'\uEC'  $'\uED'  $'\uEE'  $'\uEF'
    $'\uF0'  $'\uF1'  $'\uF2'  $'\uF3'  $'\uF4'  $'\uF5'  $'\uF6'  $'\uF7'
    $'\uF8'  $'\uF9'  $'\uFA'  $'\uFB'  $'\uFC'  $'\uFD'  $'\uFE'  $'\uFF'
)

#
# The SCRIPT_CARET_TEXT_MAP mapping array maps bytes into printable two character
# strings that can be used in the TEXT field display. The two character strings
# assigned to bytes that are Unicode C0 and C1 control codes (and DEL) all start
# with a caret (^) and end with a printable character that's selected using:
#
#       Unicode C0 and DEL: (byte + 0x40) % 0x80
#               Unicode C1: (byte + 0x40) % 0x80 + 0x80
#
# The rest of the bytes in the array are printable and the string assigned to each
# one starts with a space and ends with the Unicode character that represents that
# byte. The extension of "caret notation" beyond the ASCII block seems reasonable,
# but as far as I know it's just my own convention.
#

declare -a SCRIPT_CARET_TEXT_MAP=(
    #
    # Basic Latin Block (ASCII)
    #

    $'^\u40'  $'^\u41'  $'^\u42'  $'^\u43'  $'^\u44'  $'^\u45'  $'^\u46'  $'^\u47'
    $'^\u48'  $'^\u49'  $'^\u4A'  $'^\u4B'  $'^\u4C'  $'^\u4D'  $'^\u4E'  $'^\u4F'
    $'^\u50'  $'^\u51'  $'^\u52'  $'^\u53'  $'^\u54'  $'^\u55'  $'^\u56'  $'^\u57'
    $'^\u58'  $'^\u59'  $'^\u5A'  $'^\u5B'  $'^\u5C'  $'^\u5D'  $'^\u5E'  $'^\u5F'

    $' \u20'  $' \u21'  $' \u22'  $' \u23'  $' \u24'  $' \u25'  $' \u26'  $' \u27'
    $' \u28'  $' \u29'  $' \u2A'  $' \u2B'  $' \u2C'  $' \u2D'  $' \u2E'  $' \u2F'
    $' \u30'  $' \u31'  $' \u32'  $' \u33'  $' \u34'  $' \u35'  $' \u36'  $' \u37'
    $' \u38'  $' \u39'  $' \u3A'  $' \u3B'  $' \u3C'  $' \u3D'  $' \u3E'  $' \u3F'
    $' \u40'  $' \u41'  $' \u42'  $' \u43'  $' \u44'  $' \u45'  $' \u46'  $' \u47'
    $' \u48'  $' \u49'  $' \u4A'  $' \u4B'  $' \u4C'  $' \u4D'  $' \u4E'  $' \u4F'
    $' \u50'  $' \u51'  $' \u52'  $' \u53'  $' \u54'  $' \u55'  $' \u56'  $' \u57'
    $' \u58'  $' \u59'  $' \u5A'  $' \u5B'  $' \u5C'  $' \u5D'  $' \u5E'  $' \u5F'
    $' \u60'  $' \u61'  $' \u62'  $' \u63'  $' \u64'  $' \u65'  $' \u66'  $' \u67'
    $' \u68'  $' \u69'  $' \u6A'  $' \u6B'  $' \u6C'  $' \u6D'  $' \u6E'  $' \u6F'
    $' \u70'  $' \u71'  $' \u72'  $' \u73'  $' \u74'  $' \u75'  $' \u76'  $' \u77'
    $' \u78'  $' \u79'  $' \u7A'  $' \u7B'  $' \u7C'  $' \u7D'  $' \u7E'  $'^\u3F'

    #
    # Latin-1 Supplement Block
    #

    $'^\uC0'  $'^\uC1'  $'^\uC2'  $'^\uC3'  $'^\uC4'  $'^\uC5'  $'^\uC6'  $'^\uC7'
    $'^\uC8'  $'^\uC9'  $'^\uCA'  $'^\uCB'  $'^\uCC'  $'^\uCD'  $'^\uCE'  $'^\uCF'
    $'^\uD0'  $'^\uD1'  $'^\uD2'  $'^\uD3'  $'^\uD4'  $'^\uD5'  $'^\uD6'  $'^\uD7'
    $'^\uD8'  $'^\uD9'  $'^\uDA'  $'^\uDB'  $'^\uDC'  $'^\uDD'  $'^\uDE'  $'^\uDF'

    $' \uA0'  $' \uA1'  $' \uA2'  $' \uA3'  $' \uA4'  $' \uA5'  $' \uA6'  $' \uA7'
    $' \uA8'  $' \uA9'  $' \uAA'  $' \uAB'  $' \uAC'  $' \uAD'  $' \uAE'  $' \uAF'
    $' \uB0'  $' \uB1'  $' \uB2'  $' \uB3'  $' \uB4'  $' \uB5'  $' \uB6'  $' \uB7'
    $' \uB8'  $' \uB9'  $' \uBA'  $' \uBB'  $' \uBC'  $' \uBD'  $' \uBE'  $' \uBF'
    $' \uC0'  $' \uC1'  $' \uC2'  $' \uC3'  $' \uC4'  $' \uC5'  $' \uC6'  $' \uC7'
    $' \uC8'  $' \uC9'  $' \uCA'  $' \uCB'  $' \uCC'  $' \uCD'  $' \uCE'  $' \uCF'
    $' \uD0'  $' \uD1'  $' \uD2'  $' \uD3'  $' \uD4'  $' \uD5'  $' \uD6'  $' \uD7'
    $' \uD8'  $' \uD9'  $' \uDA'  $' \uDB'  $' \uDC'  $' \uDD'  $' \uDE'  $' \uDF'
    $' \uE0'  $' \uE1'  $' \uE2'  $' \uE3'  $' \uE4'  $' \uE5'  $' \uE6'  $' \uE7'
    $' \uE8'  $' \uE9'  $' \uEA'  $' \uEB'  $' \uEC'  $' \uED'  $' \uEE'  $' \uEF'
    $' \uF0'  $' \uF1'  $' \uF2'  $' \uF3'  $' \uF4'  $' \uF5'  $' \uF6'  $' \uF7'
    $' \uF8'  $' \uF9'  $' \uFA'  $' \uFB'  $' \uFC'  $' \uFD'  $' \uFE'  $' \uFF'
)

#
# The SCRIPT_CARET_ESCAPE_TEXT_MAP mapping array is a slightly modified version
# of SCRIPT_CARET_TEXT_MAP that uses C-style escape sequences, whenever they're
# defined, to represent control characters. The remaining control characters are
# displayed using the caret notation that's already been described.
#

declare -a SCRIPT_CARET_ESCAPE_TEXT_MAP=(
    #
    # Basic Latin Block (ASCII)
    #

      '\0'    $'^\u41'  $'^\u42'  $'^\u43'  $'^\u44'  $'^\u45'  $'^\u46'    '\a'
      '\b'      '\t'      '\n'      '\v'      '\f'      '\r'    $'^\u4E'  $'^\u4F'
    $'^\u50'  $'^\u51'  $'^\u52'  $'^\u53'  $'^\u54'  $'^\u55'  $'^\u56'  $'^\u57'
    $'^\u58'  $'^\u59'  $'^\u5A'    '\e'    $'^\u5C'  $'^\u5D'  $'^\u5E'  $'^\u5F'

    $' \u20'  $' \u21'  $' \u22'  $' \u23'  $' \u24'  $' \u25'  $' \u26'  $' \u27'
    $' \u28'  $' \u29'  $' \u2A'  $' \u2B'  $' \u2C'  $' \u2D'  $' \u2E'  $' \u2F'
    $' \u30'  $' \u31'  $' \u32'  $' \u33'  $' \u34'  $' \u35'  $' \u36'  $' \u37'
    $' \u38'  $' \u39'  $' \u3A'  $' \u3B'  $' \u3C'  $' \u3D'  $' \u3E'  $' \u3F'
    $' \u40'  $' \u41'  $' \u42'  $' \u43'  $' \u44'  $' \u45'  $' \u46'  $' \u47'
    $' \u48'  $' \u49'  $' \u4A'  $' \u4B'  $' \u4C'  $' \u4D'  $' \u4E'  $' \u4F'
    $' \u50'  $' \u51'  $' \u52'  $' \u53'  $' \u54'  $' \u55'  $' \u56'  $' \u57'
    $' \u58'  $' \u59'  $' \u5A'  $' \u5B'  $' \u5C'  $' \u5D'  $' \u5E'  $' \u5F'
    $' \u60'  $' \u61'  $' \u62'  $' \u63'  $' \u64'  $' \u65'  $' \u66'  $' \u67'
    $' \u68'  $' \u69'  $' \u6A'  $' \u6B'  $' \u6C'  $' \u6D'  $' \u6E'  $' \u6F'
    $' \u70'  $' \u71'  $' \u72'  $' \u73'  $' \u74'  $' \u75'  $' \u76'  $' \u77'
    $' \u78'  $' \u79'  $' \u7A'  $' \u7B'  $' \u7C'  $' \u7D'  $' \u7E'    '\?'

    #
    # Latin-1 Supplement Block
    #

    $'^\uC0'  $'^\uC1'  $'^\uC2'  $'^\uC3'  $'^\uC4'  $'^\uC5'  $'^\uC6'  $'^\uC7'
    $'^\uC8'  $'^\uC9'  $'^\uCA'  $'^\uCB'  $'^\uCC'  $'^\uCD'  $'^\uCE'  $'^\uCF'
    $'^\uD0'  $'^\uD1'  $'^\uD2'  $'^\uD3'  $'^\uD4'  $'^\uD5'  $'^\uD6'  $'^\uD7'
    $'^\uD8'  $'^\uD9'  $'^\uDA'  $'^\uDB'  $'^\uDC'  $'^\uDD'  $'^\uDE'  $'^\uDF'

    $' \uA0'  $' \uA1'  $' \uA2'  $' \uA3'  $' \uA4'  $' \uA5'  $' \uA6'  $' \uA7'
    $' \uA8'  $' \uA9'  $' \uAA'  $' \uAB'  $' \uAC'  $' \uAD'  $' \uAE'  $' \uAF'
    $' \uB0'  $' \uB1'  $' \uB2'  $' \uB3'  $' \uB4'  $' \uB5'  $' \uB6'  $' \uB7'
    $' \uB8'  $' \uB9'  $' \uBA'  $' \uBB'  $' \uBC'  $' \uBD'  $' \uBE'  $' \uBF'
    $' \uC0'  $' \uC1'  $' \uC2'  $' \uC3'  $' \uC4'  $' \uC5'  $' \uC6'  $' \uC7'
    $' \uC8'  $' \uC9'  $' \uCA'  $' \uCB'  $' \uCC'  $' \uCD'  $' \uCE'  $' \uCF'
    $' \uD0'  $' \uD1'  $' \uD2'  $' \uD3'  $' \uD4'  $' \uD5'  $' \uD6'  $' \uD7'
    $' \uD8'  $' \uD9'  $' \uDA'  $' \uDB'  $' \uDC'  $' \uDD'  $' \uDE'  $' \uDF'
    $' \uE0'  $' \uE1'  $' \uE2'  $' \uE3'  $' \uE4'  $' \uE5'  $' \uE6'  $' \uE7'
    $' \uE8'  $' \uE9'  $' \uEA'  $' \uEB'  $' \uEC'  $' \uED'  $' \uEE'  $' \uEF'
    $' \uF0'  $' \uF1'  $' \uF2'  $' \uF3'  $' \uF4'  $' \uF5'  $' \uF6'  $' \uF7'
    $' \uF8'  $' \uF9'  $' \uFA'  $' \uFB'  $' \uFC'  $' \uFD'  $' \uFE'  $' \uFF'
)

#
# Back to the script's preferred locale.
#

LC_ALL="${SCRIPT_LC_ALL[INTERNAL]}"

#
# If we need a mapping array to display the BYTE field the way the user requested
# (e.g., by displaying bytes in a base that xxd doesn't support or adding color),
# then the SCRIPT_BYTE_MAP array is built when Initialize7_Maps is called during
# initialization, and it's used by the ByteMapper function, exactly the way text
# mapping arrays are used.
#
# NOTE - this is a different approach than what was used to build the TEXT field
# mapping arrays. That's because all 256 elements in any BYTE field mapping array
# are built the same way, and that means each array is simple to initialize using
# bash's brace expansion.
#

declare -a SCRIPT_BYTE_MAP=()

#
# Values stored in the SCRIPT_ANSI_ESCAPE associative array are the ANSI escape
# sequences used to selectively change the foreground and background attributes
# (primarily colors) of character strings displayed in the BYTE and TEXT fields.
# They're used by Initialize8_Attributes to surround individual character strings
# in the BYTE or TEXT field mapping arrays with the ANSI escape sequences that
# enable and then disable (i.e., reset) the requested attribute.
#
# The values assigned to the keys defined in SCRIPT_ANSI_ESCAPE that start with
# FOREGROUND are ANSI escape sequences that set foreground attributes, while the
# the values assigned to the keys that start with BACKGROUND are the ANSI escape
# that set background attributes. Values are all strings that are created using
# Bash's ANSI-C style quoting (i.e., $'string'), which automatically translates
# each occurrence of \e into the unprintable escape character that starts every
# ANSI escape sequence. Take a look at
#
#     https://en.wikipedia.org/wiki/ANSI_escape_code
#
# if you want more information about ANSI escape codes.
#

declare -Ar SCRIPT_ANSI_ESCAPE=(
    #
    # Foregound color escape sequences.
    #

    [FOREGROUND.black]=$'\e[30m'
    [FOREGROUND.red]=$'\e[31m'
    [FOREGROUND.green]=$'\e[32m'
    [FOREGROUND.yellow]=$'\e[33m'
    [FOREGROUND.blue]=$'\e[34m'
    [FOREGROUND.magenta]=$'\e[35m'
    [FOREGROUND.cyan]=$'\e[36m'
    [FOREGROUND.white]=$'\e[37m'

    [FOREGROUND.alt-black]=$'\e[90m'
    [FOREGROUND.alt-red]=$'\e[91m'
    [FOREGROUND.alt-green]=$'\e[92m'
    [FOREGROUND.alt-yellow]=$'\e[93m'
    [FOREGROUND.alt-blue]=$'\e[94m'
    [FOREGROUND.alt-magenta]=$'\e[95m'
    [FOREGROUND.alt-cyan]=$'\e[96m'
    [FOREGROUND.alt-white]=$'\e[97m'

    [FOREGROUND.bright-black]=$'\e[1;30m'
    [FOREGROUND.bright-red]=$'\e[1;31m'
    [FOREGROUND.bright-green]=$'\e[1;32m'
    [FOREGROUND.bright-yellow]=$'\e[1;33m'
    [FOREGROUND.bright-blue]=$'\e[1;34m'
    [FOREGROUND.bright-magenta]=$'\e[1;35m'
    [FOREGROUND.bright-cyan]=$'\e[1;36m'
    [FOREGROUND.bright-white]=$'\e[1;37m'

    #
    # Blinking foreground color escape sequences.
    #

    [FOREGROUND.blink-black]=$'\e[5;30m'
    [FOREGROUND.blink-red]=$'\e[5;31m'
    [FOREGROUND.blink-green]=$'\e[5;32m'
    [FOREGROUND.blink-yellow]=$'\e[5;33m'
    [FOREGROUND.blink-blue]=$'\e[5;34m'
    [FOREGROUND.blink-magenta]=$'\e[5;35m'
    [FOREGROUND.blink-cyan]=$'\e[5;36m'
    [FOREGROUND.blink-white]=$'\e[5;37m'

    [FOREGROUND.blink-alt-black]=$'\e[5;90m'
    [FOREGROUND.blink-alt-red]=$'\e[5;91m'
    [FOREGROUND.blink-alt-green]=$'\e[5;92m'
    [FOREGROUND.blink-alt-yellow]=$'\e[5;93m'
    [FOREGROUND.blink-alt-blue]=$'\e[5;94m'
    [FOREGROUND.blink-alt-magenta]=$'\e[5;95m'
    [FOREGROUND.blink-alt-cyan]=$'\e[5;96m'
    [FOREGROUND.blink-alt-white]=$'\e[5;97m'

    [FOREGROUND.blink-bright-black]=$'\e[5;1;30m'
    [FOREGROUND.blink-bright-red]=$'\e[5;1;31m'
    [FOREGROUND.blink-bright-green]=$'\e[5;1;32m'
    [FOREGROUND.blink-bright-yellow]=$'\e[5;1;33m'
    [FOREGROUND.blink-bright-blue]=$'\e[5;1;34m'
    [FOREGROUND.blink-bright-magenta]=$'\e[5;1;35m'
    [FOREGROUND.blink-bright-cyan]=$'\e[5;1;36m'
    [FOREGROUND.blink-bright-white]=$'\e[5;1;37m'

    #
    # The ANSI escape code that restores the default foreground color is
    #
    #    $'\e[39m'
    #
    # but in our implementation, an empty string accomplishes the same thing and
    # is a much better choice.
    #

    [FOREGROUND.reset]=""

    #
    # Background color escape sequences - background blinking isn't possible.
    #

    [BACKGROUND.black]=$'\e[40m'
    [BACKGROUND.red]=$'\e[41m'
    [BACKGROUND.green]=$'\e[42m'
    [BACKGROUND.yellow]=$'\e[43m'
    [BACKGROUND.blue]=$'\e[44m'
    [BACKGROUND.magenta]=$'\e[45m'
    [BACKGROUND.cyan]=$'\e[46m'
    [BACKGROUND.white]=$'\e[47m'

    [BACKGROUND.alt-black]=$'\e[100m'
    [BACKGROUND.alt-red]=$'\e[101m'
    [BACKGROUND.alt-green]=$'\e[102m'
    [BACKGROUND.alt-yellow]=$'\e[103m'
    [BACKGROUND.alt-blue]=$'\e[104m'
    [BACKGROUND.alt-magenta]=$'\e[105m'
    [BACKGROUND.alt-cyan]=$'\e[106m'
    [BACKGROUND.alt-white]=$'\e[107m'

    [BACKGROUND.bright-black]=$'\e[1;40m'
    [BACKGROUND.bright-red]=$'\e[1;41m'
    [BACKGROUND.bright-green]=$'\e[1;42m'
    [BACKGROUND.bright-yellow]=$'\e[1;43m'
    [BACKGROUND.bright-blue]=$'\e[1;44m'
    [BACKGROUND.bright-magenta]=$'\e[1;45m'
    [BACKGROUND.bright-cyan]=$'\e[1;46m'
    [BACKGROUND.bright-white]=$'\e[1;47m'

    #
    # The ANSI escape code that restores the default background color is
    #
    #    $'\e[49m'
    #
    # but in our implementation, an empty string accomplishes the same thing and
    # is a much better choice.
    #

    [BACKGROUND.reset]=""

    #
    # Reset all escape sequences. Omitting the 0 should work, but decided against
    # it - at least for now.
    #

    [RESET.attributes]=$'\e[0m'
)

#
# The four arrays defined next are used to record the "attributes" (think colors)
# that are assigned to individual bytes by command line options. Each byte in the
# input file can be represented by strings that are displayed in the BYTE and TEXT
# fields in our dump, and the characters in those strings have separate BACKGROUND
# and FOREGROUND layers that can be individually "colored" by command line options.
#
# Using four separate arrays to manage attributes, rather than somehow collecting
# them all in a single array, simplifies option processing and guarantees there's
# at most one attribute associated with any byte index in each of the four arrays.
# That means if the option handling code knows the field name (BYTE or TEXT) and
# name of the layer (BACKGROUND or FOREGROUND) it knows the name of the attribute
# array that it's supposed to use, and new attributes can just replace whatever's
# currently stored there and everything should work.
#
# That wouldn't be the case if we used a single array to store the background and
# foreground attributes, because in that implementation, the option handling code
# would have to carefully examine and update any nonempty string already assigned
# to the byte that's targeted by the option. The source of the complications, and
# the real reason why we settled on using separate top-level attribute arrays, is
# because bash arrays can only contain strings.
#

declare -a SCRIPT_ATTRIBUTES_BYTE_BACKGROUND=()
declare -a SCRIPT_ATTRIBUTES_BYTE_FOREGROUND=()
declare -a SCRIPT_ATTRIBUTES_TEXT_BACKGROUND=()
declare -a SCRIPT_ATTRIBUTES_TEXT_FOREGROUND=()

#
# After the command line options are processed, any attributes stored in the four
# arrays are given a prefix (e.g., "BYTE_" or "TEXT_") that identifies the field
# and the attributes associated with each byte in those arrays are combined in a
# space separated lists that are stored in the SCRIPT_ATTRIBUTES array. It's done
# early in the initialization that follows the options primarily because a single
# array is a little easier to use when attributes are actually applied to mapping
# arrays in Initialize8_Attributes.
#
# NOTE - there are a few debug settings that can help if you're trying to follow
# the attribute related code. If you have access to a Linux system you should be
# able to run this script, and in that case something like
#
#     ./bytedump --debug=foreground,background,attributes,bytemap,textmap /dev/null
#
# will trigger debugging code that dumps the foreground and background attribute
# arrays and the BYTE and TEXT field mapping arrays, whenever any of them aren't
# empty. In this case nothing prints because the input file (i.e., /dev/null) and
# all four of the arrays are empty. However, add options like
#
#     --foreground='bright-green:0x30-0x39'
#
# or
#
#     --byte-background='bright-red:0x35-0x39'
#
# (or both) to that command line, and the debugging code will show you what's in
# the nonempty attribute and mapping arrays after they've been initialized.
#

declare -a SCRIPT_ATTRIBUTES=()

#
# Command line options used to run the xxd command end up in SCRIPT_XXD_OPTIONS.
# They're added to that array by Initialize5_XXD, which is called after most of
# the initialization is completed, and used when DumpXXD calls xxd.
#

declare -a SCRIPT_XXD_OPTIONS=()

#
# The number of command line arguments that were consumed as options is stored
# in SCRIPT_ARGUMENTS_CONSUMED. It's set in Options and used by Main to get to
# the non-option arguments. There are other solutions, but command substitution
# is not one of them - running the Options function in a subshell doesn't work.
#

declare SCRIPT_ARGUMENTS_CONSUMED="0"

##############################
#
# Script Functions
#
##############################

Arguments() {
    #
    # Expects at most one argument, which must be "-" or the name of a readable
    # file that's not a directory. Standard input is read when there aren't any
    # arguments or when "-" is the only argument. A representation of the bytes
    # in the input file are written to standard output in a style controlled by
    # the command line options.
    #
    # Treating "-" as an abbreviation for standard input, before checking to see
    # if it's the name of a readable file or directory in the current directory,
    # matches the way Linux commands typically handle it. A pathname containing
    # at least one "/" (e.g., ./-) is the way you reference a file named "-" on
    # the command line.
    #

    if (( $# <= 1 )); then
        if [[ ${1:--} == "-" ]] || [[ -r $1 ]]; then
            #
            # Check for "-" again, in case there's a directory with that name.
            #
            if [[ ${1:--} == "-" ]] || [[ ! -d $1 ]]; then
                if [[ ${SCRIPT_STRINGS[DEBUG.time]} == "TRUE" ]]; then
                    #
                    # We're only interested in timing the dump, so the options
                    # and initialization code have been intentionally skipped.
                    #
                    time Dump "${1:--}"
                else
                    Dump "${1:--}"
                fi
            else
                Error "argument ${1@Q} is a directory"
            fi
        else
            Error "argument ${1@Q} isn't a readable file"
        fi
    else
        Error "too many non-option command line arguments ${*@Q}"
    fi
}

ByteMapper() {
    local mapper_base
    local mapper_bytes
    local -i mapper_index
    local -n mapper_map
    local mapper_map_name
    local -n mapper_output
    local mapper_output_name
    local mapper_separator
    local mapper_status

    #
    # Called to translate a line of bytes that was generated by xxd into the BYTE
    # or TEXT fields in the dump that we produce. If you're not familiar with xxd
    # output try running the command:
    #
    #     xxd -c 16 -g 1 -u /etc/hosts          # or any other small file
    #
    # The hex digits extracted from the "middle" of each line of xxd output would
    # be this function's first argument. The base is the second argument, and for
    # this xxd example output it would be "HEX-UPPER", but either "HEX-LOWER" or
    # "BINARY" are also possible. The next two arguments are the "mapping" array
    # name that's used to translate the xxd generated bytes and the name of the
    # variable where the caller wants the translated bytes stored. Both are used
    # to initialize bash nameref variables. The translation works by converting
    # each xxd generated byte to a decimal number that's used as the index in the
    # mapping array where the official representation of that byte is stored. The
    # last argument is a string that's used to separate the translated bytes.
    #
    # NOTE - this can be a heavily used function, so eliminating as much overhead
    # as possible, particularly in the two while loops that do most of the work,
    # is important. Using a nameref variable to return our answer to the caller
    # makes a big difference. Unrolling the first iteration of each while loop
    # helps some because it means all those loops have to do is use whatever is
    # stored in mapper_separator. There probably are other small improvements,
    # but this function is only called twice, so replacing those two calls with
    # the code that actually does the mapping might be the easiest way to speed
    # things up a little.
    #
    # NOTE - namerefs are convenient way to handle different mapping arrays and
    # they're an efficient way to return the translation to the caller, but they
    # introduce issues that should be appreciated. For example, everything breaks
    # if this function has a local variable with a name that matches any of the
    # caller's variable names that are used to initialize the function's nameref
    # variables. There's no way to completely prevent this kind of accident, but
    # starting all local variable names with a prefix, like "mapper_", means at
    # least there's a simple rule the caller can follow to avoid them.
    #
    # NOTE - namerefs created using strings that users can control (e.g., command
    # line arguments, environment variables, content of files) have to be treated
    # very carefully, because there's a nameref related security hole that can be
    # exploited if your bash script is careless. I found it all pretty surprising
    # and you might too. If you're curious, take a look at
    #
    #     https://mywiki.wooledge.org/BashFAQ/048
    #
    # then search for the heading
    #
    #     The problem with bash's name references
    #
    # and you'll find a discussion of some of the issues that are associated with
    # bash namerefs. It's a short section, with a two line example that shows how
    # careless use of nameref variables can result in the execution of code that's
    # hidden in a command line argument. That example forces the execution of the
    # date command, which is convenient because the output of date is recorded on
    # the webpage and it suggests this is a security issue that was known in 2014.
    # Unfortunately, the example fools the version of bash that's installed on my
    # system, so this is still an issue bash programmers should be aware of.
    #
    # The way I understand things, which may or may not be completely correct, is
    # that this flavor of "arbitrary code execution" only happens when a string is
    # assigned to an uninitialized variable that has the nameref attribute. If I'm
    # right, then the problem can be eliminated by making sure strings assigned to
    # uninitialized nameref variables always look like valid bash variable names.
    # Even though it's a simple check, it's not currently necessary because every
    # single nameref variable in this script is always initialized using a string
    # that's completely controlled by the script (and not the user).
    #
    # NOTE - mapper_index needs to be an integer, because we want bash to handle
    # the conversion of hex or binary representations of bytes to decimal numbers
    # that can be used as indices into the mapping array. Base conversion syntax
    # is described in bash documentation (i.e., the man page or reference manual)
    # in the section devoted to arithmetic. Translating the binary representation
    # of bytes in xxd output probably means there isn't an easy alternative.
    #

    mapper_status="1"

    mapper_bytes="$1"
    mapper_base="$2"
    mapper_map_name="$3"
    mapper_output_name="$4"
    mapper_separator="$5"                       # optional separator

    mapper_map="$mapper_map_name"               # create the map nameref
    mapper_output="$mapper_output_name"         # create the output nameref

    if [[ -n $mapper_bytes && -n $mapper_base ]]; then
        mapper_output=""
        case "${mapper_base^^}" in
            BINARY)
                if [[ $mapper_bytes =~ ^[[:blank:]]*([01]{8})[[:blank:]]* ]]; then
                    #
                    # Unrolled first loop interation because it's the only one
                    # that doesn't use $mapper_separator.
                    #
                    mapper_index="2#${BASH_REMATCH[1]}"         # bash base conversion
                    mapper_output="${mapper_map[$mapper_index]}"
                    mapper_bytes="${mapper_bytes:${#BASH_REMATCH[0]}}"
                    while [[ $mapper_bytes =~ ^([01]{8})[[:blank:]]* ]]; do
                        mapper_index="2#${BASH_REMATCH[1]}"
                        mapper_output+="${mapper_separator}${mapper_map[$mapper_index]}"
                        mapper_bytes="${mapper_bytes:${#BASH_REMATCH[0]}}"
                    done
                fi;;

            HEX-LOWER|HEX-UPPER)
                if [[ $mapper_bytes =~ ^[[:blank:]]*([[:xdigit:]]{2})[[:blank:]]* ]]; then
                    #
                    # Unrolled first loop interation because it's the only one
                    # that doesn't use $mapper_separator.
                    #
                    mapper_index="16#${BASH_REMATCH[1]}"        # bash base conversion
                    mapper_output="${mapper_map[$mapper_index]}"
                    mapper_bytes="${mapper_bytes:${#BASH_REMATCH[0]}}"
                    while [[ $mapper_bytes =~ ^([[:xdigit:]]{2})[[:blank:]]* ]]; do
                        mapper_index="16#${BASH_REMATCH[1]}"
                        mapper_output+="${mapper_separator}${mapper_map[$mapper_index]}"
                        mapper_bytes="${mapper_bytes:${#BASH_REMATCH[0]}}"
                    done
                fi;;

             *) InternalError "base ${mapper_base@Q} has not been implemented";;
        esac
        if [[ -z $mapper_bytes ]]; then
            mapper_status="0"
        else
            InternalError "parsing input argument ${1@Q} failed at ${mapper_bytes@Q}"
        fi
    fi

    return "$mapper_status"
}

ByteSelector() {
    local selector_attribute
    local selector_base
    local selector_body
    local selector_char_code
    local -a selector_chars
    local selector_class
    local -i selector_first
    local selector_hashes
    local selector_index
    local selector_input
    local selector_input_start
    local -i selector_last
    local selector_lc_all
    local -n selector_output
    local selector_output_name
    local selector_prefix
    local selector_quote
    local selector_status
    local selector_suffix
    local selector_tail

    #
    # Called to parse a string that's supposed to assign an attribute (primarily
    # a color) to a group of bytes whenever any of them is displayed in the BYTE
    # or TEXT fields of the dump that this program produces. There's some simple
    # recursion used to implement "character classes" and "raw strings", but the
    # initial call is always triggered by a command line option.
    #
    # The first argument is a string that identifies the attribute that the user
    # wants applied to the bytes selected by the second argument. This function's
    # job is to figure out the numeric values of the selected bytes and associate
    # the attribute (i.e., the first argument) with each byte's numeric value in
    # the array that's named by the third argument. The first and third arguments
    # should be safe, at least in all current ByteSelector calls.
    #
    # The second argument is the byte "selector", and in this function's initial
    # call it always comes directly from a command line option, so it needs lots
    # of careful error checking. The selector consists of space separated tokens
    # that are extracted using bash regular expressions. The accepted tokens are
    # integers, integer ranges, character classes, and a modified implementation
    # of Rust raw string literals.
    #
    # A selector string that starts with an optional base prefix and is followed
    # by tokens that are completely enclosed in a single set of parentheses picks
    # the base used to evaluate all numbers in the selector. A base prefix that's
    # "0x" or "0X" means all numbers are hex, "0" means they're all octal, and no
    # base prefix (just the parens) means they're all decimal. Setting the default
    # base this way, instead of using an option, makes it easy for the user to do
    # exactly the same thing from the command line.
    #
    # If a base is set, all characters in an integer token must be digits in that
    # base. Otherwise C-style syntax is used, so hex integers start with "0x" or
    # "0X", octal integers start with "0", and decimal integers always start with
    # a nonzero decimal digit. An integer range is a pair of integers separated
    # by '-'. It represents a closed interval that extends from the left integer
    # to the right integer. Both end points of a range must be expressed in the
    # same base. Any integer or any part of an integer range that doesn't fit in
    # a byte is ignored.
    #
    # A character class uses a short, familiar lowercase name to select a group
    # of bytes. Those names must be bracketed by "[:" and ":]" in the selector
    # to be recognized as a character class. The 15 character classes that are
    # allowed in a selector are:
    #
    #     [:alnum:]      [:digit:]      [:punct:]
    #     [:alpha:]      [:graph:]      [:space:]
    #     [:blank:]      [:lower:]      [:upper:]
    #     [:cntrl:]      [:print:]      [:xdigit:]
    #
    #     [:ascii:]      [:latin1:]     [:all:]
    #
    # The first four rows are the 12 character classes that are defined in the
    # POSIX standard. The last row are 3 character classes that we decided to
    # support because they seemed like a convenient way to select familiar (or
    # otherwise obvious) blocks of contiguous bytes. This script only deals with
    # bytes, so it's easy to enumerate their members using integers and integer
    # ranges, and that's exactly how this function uses recursion to implement
    # character classes.
    #
    # A modified version of Rust's raw string literal can also be used as a token
    # in the byte selector. They always start with a prefix that's the letter 'r',
    # zero or more '#' characters, and a single or double quote, and they end with
    # a suffix that matches the quote and the number of '#' characters used in the
    # prefix. For example,
    #
    #       r"hello, world"
    #       r'hello, world'
    #      r#'hello, world'#
    #     r##"hello, world"##
    #
    # are valid selectors that represent exactly the same string. Any character,
    # except null, can appear in a raw string that's used as a selector, and the
    # selected bytes are the Unicode code points of the characters in the string
    # that are less than 256.
    #
    # NOTE - selector_first and selector_last need to be integers, because we use
    # bash's base conversion syntax to convert hex and octal numbers to decimal.
    # However, unlike ByteMapper, this function isn't used much and doesn't have
    # to deal with binary numbers, so printf would be an easy alternative.
    #
    # NOTE - recent locale related change (that hasn't been well tested but seems
    # to work) immediately saves the value assigned to LC_ALL and then switches to
    # the user's locale. Selections are all made using that locale and right before
    # returning the value of LC_ALL saved when the function started is stored back
    # in LC_ALL. Needs more testing, but seems to handle recursion and restoration
    # of LC_ALL properly.
    #
    # NOTE - there appear to be different opinions about several of the HEX lists
    # that are used to implement "character classes" in the recursive ByteSelector
    # calls in this function. ChatGPT, when asked carefully about those HEX lists,
    # seems to think "[:lower:]", "[:upper:]", and "[:punct:]" aren't all correct.
    # However, I've now included a short bash script (named charclass) that can be
    # used to build those HEX lists by using the POSIX character classes that are
    # available in bash regular expressions, and in all cases the output of that
    # script agrees with the HEX lists that are hardcoded in this function. It's
    # definitely not urgent, but eventually deserves a closer look.
    #

    selector_lc_all="$LC_ALL"
    LC_ALL="${SCRIPT_LC_ALL[EXTERNAL]}"

    selector_status="0"
    selector_base=""

    selector_attribute="$1"
    selector_input="$2"
    selector_output_name="$3"

    #
    # First check for the optional base prefix.
    #

    if [[ $selector_input =~ ^[[:blank:]]*(0[xX]?)?"("(.*)")"[[:blank:]]*$ ]]; then
        #
        # Selector string starts with an optional base prefix and is followed by
        # tokens that are completely enclosed in a single set of parentheses, so
        # set the default base and update the selector string.
        #
        selector_prefix="${BASH_REMATCH[1]}"
        selector_input="${BASH_REMATCH[2]}"

        case "$selector_prefix" in
            0[xX]) selector_base="16";;
                0) selector_base="8";;
               "") selector_base="10";;
                *) InternalError "parser prefix ${selector_prefix@Q} has not been implemented";;
        esac
    fi

    if [[ $selector_output_name =~ ^SCRIPT_ATTRIBUTES_(BYTE|TEXT)_(BACKGROUND|FOREGROUND)$ ]]; then
        selector_output="$selector_output_name"     # create the output nameref

        while [[ $selector_input =~ ^[[:blank:]]*([^[:blank:]].*) ]]; do
            #
            # Omitting leading blanks here means the regular expressions that do the
            # real work don't have to worry about them. Decided to save a copy of the
            # initial input, because selector_input might be modified when we want to
            # use it in an error message.
            #
            selector_input="${BASH_REMATCH[1]}"
            selector_input_start="$selector_input"      # for error messages

            #
            # Implemented tokens can be identified by looking at how they start. It's
            # an approach that also improves error messages when we notice mistakes.
            #
            if [[ $selector_input =~ ^(0[xX]?)?[[:xdigit:]] ]]; then
                #
                # Next token looks like an integer or integer range. All "numbers" are
                # carefully checked before we ask bash to do anything significant with
                # them.
                #
                if [[ -n $selector_base ]]; then
                    #
                    # When there's a base, prefixes (i.e., 0x or 0) aren't recognized
                    # and the digits in every integer must all be valid in that base.
                    #
                    if [[ $selector_base == "16" ]]; then
                        if [[ $selector_input =~ ^(([[:xdigit:]]+)([-]([[:xdigit:]]+))?)([[:blank:]]+|$) ]]; then
                            selector_first="16#${BASH_REMATCH[2]}"
                            selector_last="16#${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                            selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                        else
                            Error "problem extracting a hex integer from ${selector_input_start@Q}"
                        fi
                    elif [[ $selector_base == "8" ]]; then
                        if [[ $selector_input =~ ^(([01234567]+)([-]([01234567]+))?)([[:blank:]]+|$) ]]; then
                            selector_first="8#${BASH_REMATCH[2]}"
                            selector_last="8#${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                            selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                        else
                            Error "problem extracting an octal integer from ${selector_input_start@Q}"
                        fi
                    elif [[ $selector_base == "10" ]]; then
                        if [[ $selector_input =~ ^(([123456789][0123456789]*)([-]([123456789][0123456789]*))?)([[:blank:]]+|$) ]]; then
                            selector_first="${BASH_REMATCH[2]}"
                            selector_last="${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                            selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                        else
                            Error "problem extracting a decimal integer from ${selector_input_start@Q}"
                        fi
                    else
                        InternalError "base ${selector_base@Q} has not been implemented"
                    fi
                else
                    #
                    # There's no default base, so all integers and integer ranges must
                    # use C-style literal notation to specify the base. Both ends of an
                    # integer range must be expressed in the same base.
                    #
                    if [[ $selector_input =~ ^(0[xX]([[:xdigit:]]+)([-]0[xX]([[:xdigit:]]+))?)([[:blank:]]+|$) ]]; then
                        selector_first="16#${BASH_REMATCH[2]}"
                        selector_last="16#${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                        selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                    elif [[ $selector_input =~ ^((0[01234567]*)([-](0[01234567]*))?)([[:blank:]]+|$) ]]; then
                        selector_first="8#${BASH_REMATCH[2]}"
                        selector_last="8#${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                        selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                    elif [[ $selector_input =~ ^(([123456789][0123456789]*)([-]([123456789][0123456789]*))?)([[:blank:]]+|$) ]]; then
                        selector_first="${BASH_REMATCH[2]}"
                        selector_last="${BASH_REMATCH[4]:-${BASH_REMATCH[2]}}"
                        selector_input="${selector_input:${#BASH_REMATCH[0]}}"
                    else
                        Error "problem extracting an integer from ${selector_input_start@Q}"
                    fi
                fi

                if (( selector_first <= selector_last && selector_first < 256 )); then
                    if (( selector_last > 256 )); then
                        selector_last="256"
                    fi
                    for (( selector_index = selector_first; selector_index <= selector_last; selector_index++ )); do
                        selector_output[${selector_index}]="$selector_attribute"
                    done
                fi
            elif [[ $selector_input =~ ^"[:" ]]; then
                #
                # Next token looks like a "character class". All the names must be
                # alphanumeric and bracketed by "[:" and ":]". They'll also have to
                # be recognized in the case statement, so they're basically checked
                # twice.
                #
                if [[ $selector_input =~ ^"[:"([[:alnum:]]+)":]"([[:blank:]]+|$) ]]; then
                    selector_class="${BASH_REMATCH[1]}"
                    selector_input="${selector_input:${#BASH_REMATCH[0]}}"

                    #
                    # The case statement recognizes the 12 character class names defined in
                    # the POSIX standard, plus 3 custom character class names that seem like
                    # a convenient way to select familiar (or otherwise obvious) contiguous
                    # blocks of bytes.
                    #
                    # NOTE - ChatGPT seems to think HEX lists used to implement the "lower",
                    # "upper", and "punct" character classes aren't completely correct. Take
                    # a look at the charclass.sh script that's now included with the source
                    # code for more details.
                    #
                    case "$selector_class" in
                        #
                        # POSIX character class names.
                        #
                         "alnum") ByteSelector "$selector_attribute" "0x(30-39 41-5A 61-7A AA B5 BA C0-D6 D8-F6 F8-FF)" "$selector_output_name";;
                         "alpha") ByteSelector "$selector_attribute" "0x(41-5A 61-7A AA B5 BA C0-D6 D8-F6 F8-FF)" "$selector_output_name";;
                         "blank") ByteSelector "$selector_attribute" "0x(09 20)" "$selector_output_name";;
                         "cntrl") ByteSelector "$selector_attribute" "0x(00-1F 7F-9F)" "$selector_output_name";;
                         "digit") ByteSelector "$selector_attribute" "0x(30-39)" "$selector_output_name";;
                         "graph") ByteSelector "$selector_attribute" "0x(21-7E A0-FF)" "$selector_output_name";;
                         "lower") ByteSelector "$selector_attribute" "0x(61-7A AA B5 BA DF-F6 F8-FF)" "$selector_output_name";;
                         "print") ByteSelector "$selector_attribute" "0x(20-7E A0-FF)" "$selector_output_name";;
                         "punct") ByteSelector "$selector_attribute" "0x(21-2F 3A-40 5B-60 7B-7E A0-A9 AB-B4 B6-B9 BB-BF D7 F7)" "$selector_output_name";;
                         "space") ByteSelector "$selector_attribute" "0x(09-0D 20)" "$selector_output_name";;
                         "upper") ByteSelector "$selector_attribute" "0x(41-5A C0-D6 D8-DE)" "$selector_output_name";;
                        "xdigit") ByteSelector "$selector_attribute" "0x(30-39 41-46 61-66)" "$selector_output_name";;

                        #
                        # Custom character class names.
                        #
                         "ascii") ByteSelector "$selector_attribute" "0x(00-7F)" "$selector_output_name";;
                        "latin1") ByteSelector "$selector_attribute" "0x(80-FF)" "$selector_output_name";;
                           "all") ByteSelector "$selector_attribute" "0x(00-FF)" "$selector_output_name";;

                               *) Error "${selector_class@Q} is not the name of an implemented character class";;
                    esac
                else
                    Error "problem extracting a character class from ${selector_input_start@Q}"
                fi
            elif [[ $selector_input =~ ^(r([#]*)(\"|\')) ]]; then
                #
                # Next token looks like a slightly modified Rust raw string literal.
                # It accepts matching single or double quotes in the delimiters and
                # doesn't explicitly reject any characters. Everything between the
                # the opening and closing string delimiters is accepted. After that
                # the individual characters represented by those bytes (in the user's
                # locale) are converted to Unicode code points and the ones less than
                # 256 identify the bytes that the user asked us to select.
                #
                # Once we've recognized the prefix it's easy to construct the suffix
                # that's supposed to mark the end of this "raw string". After that we
                # can use them to separate $selector_input into appropriate pieces.
                #
                # NOTE - this is pretty difficult code and really doesn't add much to
                # the program. It's here mostly because it felt like a challenge, and
                # I wanted to see if I could implement it in this bash script using a
                # purely bash solution. Removing this stuff would not be a big deal.
                #

                selector_prefix="${BASH_REMATCH[1]}"
                selector_suffix="${BASH_REMATCH[3]}${BASH_REMATCH[2]}"

                #
                # Next few steps probably are trickier than you might expect. The same
                # character (i.e., one single or double quote) could mark both ends of
                # the raw string or there could be several raw strings in the selector
                # that use the same delimiters. We also have to deal with the fact bash
                # regular expressions are "greedy", so even if it correctly handles one
                # raw string it could easily fail when there are two or more of them in
                # the selector.
                #
                # So the bottom line is we have to be careful "parsing" raw strings. The
                # first step is remove the raw string's prefix from the current selector
                # string. Once that's done we can use a regular expression to find the
                # first occurence of the raw string's suffix, and after that it's pretty
                # easy to grab everything that we need.
                #

                selector_input="${selector_input:${#selector_prefix}}"

                if [[ $selector_input =~ "${selector_suffix}"(.*) ]]; then
                    #
                    # Found the end of this raw string - whatever follows the suffix will
                    # be the input string that's processed by the next interation through
                    # the outer loop.
                    #
                    selector_tail="${BASH_REMATCH[1]}"
                    if [[ $selector_tail =~ ^([[:blank:]]|$) ]]; then
                        #
                        # Grab the body of the raw string (everything between the prefix
                        # and suffix), then update selector_input for the next loop.
                        #

                        selector_body="${selector_input:0:${#selector_input} - (${#selector_suffix} + ${#selector_tail})}"
                        selector_input="$selector_tail"

                        #
                        # Need to convert characters in $selector_body to character codes
                        # and then use the codes that are less than 256 to select bytes
                        # that are targeted by $selector_attribute. Wanted to use a purely
                        # bash solution that relies on "partially documented" behavior of
                        # the printf builtin's handling of numeric format specifiers when
                        # its argument starts with a single or double quote. For example,
                        # the output from either of the commands
                        #
                        #     printf "%d" "'X"
                        #     printf "%d" '"X'
                        #
                        # should be the ASCII code (i.e., 88) of the letter X that follows
                        # the quote. Anyway, documentation in the bash man page only talks
                        # about ASCII, but printf seems to return Unicode code points for
                        # characters that aren't in the ASCII character set.
                        #
                        # I'm reasonably confident in this approach, and would guess the
                        # only issue is the bash man page needs updating. If you disagree
                        # there are alternatives (e.g., ask python or perl to handle the
                        # conversions).
                        #

                        selector_chars=()
                        for (( selector_index = 0; selector_index < ${#selector_body}; selector_index++ )); do
                            #
                            # Have bash extract the next character from $selector_body using
                            # substring expansion, ask printf to convert it to a Unicode code
                            # point, and save the numbers that fit in a byte as two character
                            # hex strings in the selector_chars indexed array. A little later
                            # those two character hex strings are used in a recursive call of
                            # this function.
                            #
                            # NOTE - extra careful here because we're working with unchecked
                            # input that came directly from the command line.
                            #

                            selector_char_code=""               # a precaution
                            if printf -v "selector_char_code" "%d" "'${selector_body:${selector_index}:1}"; then
                                #
                                # Not using a range or character class here - just to be safe.
                                #
                                if [[ $selector_char_code =~ ^[0123456789]+$ ]]; then
                                    if (( selector_char_code >= 0 && selector_char_code < 256 )); then
                                        printf -v "selector_chars[${selector_char_code}]" "%.2X" "${selector_char_code}"
                                    fi
                                else
                                    InternalError "recovered code point ${selector_char_code@Q} of character at index ${selector_index} in ${selector_body@Q} isn't valid"
                                fi
                            else
                                InternalError "problem recovering code point of character at index ${selector_index} in ${selector_body@Q}"
                            fi
                        done

                        if (( ${#selector_chars[@]} > 0 )); then
                            ByteSelector "$selector_attribute" "0x(${selector_chars[*]})" "$selector_output_name"
                        fi
                    else
                        Error "all tokens must be space separated in byte selector ${selector_input_start@Q}"
                    fi
                else
                    Error "can't find raw string suffix ${selector_suffix@Q} in byte selector ${selector_input_start@Q}"
                fi
            else
                Error "no valid token found at the start of byte selector ${selector_input_start@Q}"
            fi
        done
    else
        InternalError "${selector_output_name@Q} is not recognized as an attribute array name"
    fi

    #
    # Restore LC_ALL to what it was when this function was called.
    #

    LC_ALL="$selector_lc_all"

    return "$selector_status"
}

Debug() {
    #
    # Takes zero or more arguments that select debugging keys and makes sure most
    # of the arguments are handled by debugging code in DebugHandler. No arguments
    # means check all the debug keys that aren't explicitly mentioned as arguments
    # in other Debug calls or completely supported by their own debugging code.
    #
    # DEBUG.locals is the only debugging key that's handled in this function. The
    # code that does the work makes lots of implicit assumptions, like global and
    # local name spaces are disjoint and easily separated, and that functions on
    # call stack haven't added their own local variables to the ones we see using
    # declare (or set). It's a simple approach with many flaws, but it sometimes
    # does help find typos or missing local variable declarations that otherwise
    # might be hard to spot.
    #
    # NOTE - originally the debugging code that's now in DebugHandler was in this
    # function's while loop. It was moved to make sure that the implementation of
    # DEBUG.locals wouldn't be "tricked" by any local variables that were used to
    # support all the other debug keys. Definitely not a big deal, but this seems
    # like a better implementation than depending on unset to clean everything up
    # for DEBUG.locals. Decided to leave the DEBUG.locals code here, even though
    # I doubt there would be a problem moving it.
    #

    if (( $# == 0 )); then
        set -- "foreground" "background" "attributes" "bytemap" "textmap" "textmap-bash" "strings"
    fi

    while (( $# > 0 )); do
        case "$1" in
            locals)
                if [[ ${SCRIPT_STRINGS[DEBUG.locals]} == "TRUE" ]]; then
                    #
                    # A useful, but flawed, check sometimes notices local variables
                    # that "leak" into the global scope because of typos or missing
                    # local declarations. Nothing about this is guaranteed to work,
                    # but it can sometimes be very useful.
                    #
                    # The first check is to avoid producing any output if it doesn't
                    # look like there are any "leaked" variables. The same check is
                    # basically repeated because we didn't want to save the original
                    # answer in a local variable.
                    #
                    if declare | LC_ALL=C command -p grep -q -- '^[a-z][a-z_]*=' 2>/dev/null; then
                        printf "[Debug] Possible Leaked Locals:\n"
                        declare | LC_ALL=C command -p grep -- '^[a-z][a-z_]*=' | command -p sed -e 's/^/[Debug]    /'
                        printf "\n"
                    fi
                fi;;

             *) DebugHandler "$@";;
        esac

        #
        # The "xxd" debugging key expects one argument (the input filename), all
        # the others currently don't.
        #

        case "$1" in
            xxd) shift 2;;
              *) shift;;
        esac
    done
}

DebugHandler() {
    local -n array
    local char
    local col
    local -A consumed_keys
    local field
    local index
    local initial_keys
    local key
    local -n map
    local -a matched_keys
    local name
    local prefix
    local quote
    local row
    local tag

    #
    # The arguments that are recognized by this function select debug keys that
    # are supposed to generate immediate output and aren't handled anywhere else
    # in the script. No looping in this function and only initializing variables
    # when they're really needed, means the DEBUG.locals code in Debug probably
    # could also be moved into this function.
    #

    case "$1" in
        attributes)
            if [[ ${SCRIPT_STRINGS[DEBUG.attributes]} == "TRUE" ]]; then
                #
                # Useful when the color support was implemented, but it probably
                # doesn't have much debugging value now. However, it could still
                # be useful if you're just trying to understand how the existing
                # implementation works.
                #
                if (( ${#SCRIPT_ATTRIBUTES[@]} > 0 )); then
                    printf "[Debug] SCRIPT_ATTRIBUTES[%d]:\n" "${#SCRIPT_ATTRIBUTES[@]}"
                    for index in "${!SCRIPT_ATTRIBUTES[@]}"; do
                        if [[ -n ${SCRIPT_ATTRIBUTES[$index]} ]]; then
                            printf "[Debug]   %5s=%s\n" "[${index}]" "${SCRIPT_ATTRIBUTES[$index]}"
                        fi
                    done
                    printf "\n"
                fi
            fi;;

        background)
            if [[ ${SCRIPT_STRINGS[DEBUG.background]} == "TRUE" ]]; then
                #
                # Useful when the background color support was implemented, but
                # it probably doesn't have much debugging value now. However, it
                # could still be useful if you're just trying to understand how
                # the existing implementation works.
                #
                for field in "BYTE" "TEXT"; do
                    name="SCRIPT_ATTRIBUTES_${field}_BACKGROUND"
                    typeset -n array="$name"
                    if (( ${#array[@]} > 0 )); then
                        printf "[Debug] %s[%d]:\n" "$name" "${#array[@]}"
                        for index in "${!array[@]}"; do
                            if [[ -n ${array[$index]} ]]; then
                                printf "[Debug]   %5s=%s\n" "[${index}]" "${array[$index]}"
                            fi
                        done
                        printf "\n"
                    fi
                done
            fi;;

        bytemap)
            if [[ ${SCRIPT_STRINGS[DEBUG.bytemap]} == "TRUE" ]]; then
                #
                # Prints the BYTE field mapping array in a 16x16 "table". It's a
                # good way to see the colors assigned to the strings that can be
                # displayed in the BYTE field.
                #
                if [[ -n ${SCRIPT_STRINGS[BYTE.map]} ]]; then
                    map="${SCRIPT_STRINGS[BYTE.map]}"
                    printf "[Debug] %s[%d]:\n" "${SCRIPT_STRINGS[BYTE.map]}" "${#map[@]}"
                    for (( row = 0; row < 16; row++ )); do
                        prefix="[Debug]    "
                        for (( col = 0; col < 16; col++ )); do
                            printf "%s%s" "$prefix" "${map[$((16*row + col))]}"
                            prefix=" "
                        done
                        printf "\n"
                    done
                    printf "\n"
                fi
            fi;;

        foreground)
            if [[ ${SCRIPT_STRINGS[DEBUG.foreground]} == "TRUE" ]]; then
                #
                # Useful when the foreground color support was implemented, but
                # it probably doesn't have much debugging value now. However, it
                # could still be useful if you're just trying to understand how
                # the existing implementation works.
                #
                for field in "BYTE" "TEXT"; do
                    name="SCRIPT_ATTRIBUTES_${field}_FOREGROUND"
                    typeset -n array="$name"
                    if (( ${#array[@]} > 0 )); then
                        printf "[Debug] %s[%d]:\n" "$name" "${#array[@]}"
                        for index in "${!array[@]}"; do
                            if [[ -n ${array[$index]} ]]; then
                                printf "[Debug]   %5s=%s\n" "[${index}]" "${array[$index]}"
                            fi
                        done
                        printf "\n"
                    fi
                done
            fi;;

        strings)
            if [[ ${SCRIPT_STRINGS[DEBUG.strings]} == "TRUE" ]]; then
                #
                # This probably is the most useful debugging output when you're
                # trying to fix a real problem. It's automatically called after
                # all the initialization is complete, but just add the line
                #
                #     Debug strings
                #
                # to the code whenever you want to see what's in SCRIPT_STRINGS.
                # You can adjust the fields that are displayed in this the dump
                # by changing SCRIPT_STRINGS[DEBUG.strings.prefixes].
                #
                # NOTE - SCRIPT_STRINGS is a big associative array, so using
                #
                #     declare -p SCRIPT_STRINGS >&2
                #
                # is quick, but I found it almost useless for tracking down bugs
                # because the keys are unsorted and they all print on one line.
                #
                consumed_keys=()
                initial_keys="${SCRIPT_STRINGS[INFO.initial.keys]}"

                printf "[Debug] SCRIPT_STRINGS[%d]:\n" "${#SCRIPT_STRINGS[@]}"
                for prefix in ${SCRIPT_STRINGS[DEBUG.strings.prefixes]}; do
                    matched_keys=()
                    for key in "${!SCRIPT_STRINGS[@]}"; do
                        if [[ $key =~ ^"${prefix}" ]] && [[ -z ${consumed_keys[$key]} ]]; then
                            matched_keys+=("$key")
                            consumed_keys[$key]="TRUE"
                        fi
                    done
                    if (( ${#matched_keys[@]} > 0 )); then
                        for key in $(printf "%s\n" "${matched_keys[@]}" | LC_ALL=C command -p sort --field-separator='.' --key=1 --key=2 --key=3); do
                            #
                            # shellcheck disable=SC2076
                            #
                            if [[ $initial_keys =~ "${key} " ]]; then
                                tag="  "
                            else
                                tag="->"            # marks a possible mistake
                            fi
                            printf "[Debug] %s %s=%s\n" "$tag" "$key" "${SCRIPT_STRINGS[$key]@Q}"
                        done
                        printf "[Debug]\n"
                    fi
                done
                printf "\n"
            fi;;

        textmap)
            if [[ ${SCRIPT_STRINGS[DEBUG.textmap]} == "TRUE" ]]; then
                #
                # Prints the TEXT field mapping array in a 16x16 "table". It's a
                # good way to see the colors assigned to the strings that can be
                # displayed in the TEXT field.
                #
                if [[ -n ${SCRIPT_STRINGS[TEXT.map]} ]]; then
                    map="${SCRIPT_STRINGS[TEXT.map]}"
                    printf "[Debug] %s[%d]:\n" "${SCRIPT_STRINGS[TEXT.map]}" "${#map[@]}"
                    for (( row = 0; row < 16; row++ )); do
                        prefix="[Debug]    "
                        for (( col = 0; col < 16; col++ )); do
                            printf "%s%s" "$prefix" "${map[$((16*row + col))]}"
                            prefix=" "
                        done
                        printf "\n"
                    done
                    printf "\n"
                fi
            fi;;

        textmap-bash)
            if [[ ${SCRIPT_STRINGS[DEBUG.textmap-bash]} == "TRUE" ]]; then
                #
                # A dump of the actual characters in the textmap array in a format
                # that bash could use to recreate that textmap array directly from
                # the output. This code doesn't add debugging capabilities to the
                # script, but I occasionally did find it convenient, so I decided
                # not to delete it. You should use the
                #
                #     --debug=textmap
                #
                # option, rather than this code, to look at the textmap array.
                #
                if [[ -n ${SCRIPT_STRINGS[TEXT.map]} ]]; then
                    map="${SCRIPT_STRINGS[TEXT.map]}"
                    printf "declare -a %s=(\n" "${SCRIPT_STRINGS[TEXT.map]}"
                    for (( row = 0; row < 16; row++ )); do
                        prefix="    "
                        for (( col = 0; col < 16; col++ )); do
                            char="${map[$((16*row + col))]}"
                            if [[ $char =~ "'" ]]; then
                                quote='"'
                            else
                                quote="'"
                            fi
                            printf "%s%s%s%s" "$prefix" "$quote" "$char" "$quote"
                            prefix=" "
                        done
                        printf "\n"
                    done
                    printf ")\n"
                fi
            fi;;

        xxd)                # should be followed by the input file name
            if [[ ${SCRIPT_STRINGS[DEBUG.xxd]} == "TRUE" ]]; then
                #
                # Displays the full command line used to run xxd. The output is
                # labeled "Dump Preprocessor" if xxd is piped into the internal
                # dump function, otherwise it's labeled "Dump Generator".
                #
                case "${SCRIPT_STRINGS[DUMP.handler]}" in
                    DUMP-INTERNAL)
                        printf "[Debug] Dump Preprocessor: xxd %s\n" "${SCRIPT_XXD_OPTIONS[*]} ${2:--}";;

                    DUMP-XXD)
                        printf "[Debug] Dump Generator: xxd %s\n" "${SCRIPT_XXD_OPTIONS[*]} ${2:--}";;

                     *) InternalError "dump ${SCRIPT_STRINGS[DUMP.handler]@Q} has not been implemented";;
                esac
                printf "\n"
            fi;;
    esac >&2
}

Dump() {
    local handler

    #
    # Calls the dump handler that was selected during initialization. There's not
    # much going on here, and I doubt that will ever change, but if we did want
    # to plug in another dump generator (e.g., od or hexdump), this is one place
    # it probably could be done.
    #
    # NOTE - the selection of the "handler" was originally done by modifying the
    # the value stored in SCRIPT_STRINGS[DEBUG.dump]. That approach got a little
    # more complicated after I decided the SCRIPT_STRINGS array would be readonly
    # after all the initialization finished. No big deal either way.
    #

    if [[ -n ${SCRIPT_STRINGS[DUMP.handler]} ]]; then
        handler="${SCRIPT_STRINGS[DUMP.handler]}"

        if [[ -n ${SCRIPT_STRINGS[DEBUG.dump]} ]]; then
            #
            # We only get here because the --debug-dump option was used. It lets
            # the user override the dump function that Initialize6_Handler picked
            # and also prints a short summary of what's happening.
            #
            printf "[Debug]  Selected Dump: %s\n" "$handler"
            printf "[Debug] Requested Dump: %s\n" "${SCRIPT_STRINGS[DEBUG.dump]}"

            case "${SCRIPT_STRINGS[DEBUG.dump]}" in
                internal) handler="DUMP-INTERNAL";;
                selected) ;;
                     xxd) handler="DUMP-XXD";;
                       *) InternalError "DEBUG.dump ${SCRIPT_STRINGS[DEBUG.dump]@Q} has not been implemented";;
            esac

            printf "[Debug]      Used Dump: %s\n" "$handler"
            printf "\n"
        fi >&2

        case "$handler" in
            DUMP-INTERNAL)
                DumpXXDInternal "${1:--}";;

            DUMP-XXD)
                DumpXXD "${1:--}";;

             *) InternalError "dump handler ${SCRIPT_STRINGS[DUMP.handler]@Q} has not been implemented";;
        esac
    else
        InternalError "SCRIPT_STRINGS[DUMP.handler] has not been initialized"
    fi
}

DumpXXD() {
    #
    # Runs the xxd command using options that were stored in SCRIPT_XXD_OPTIONS by
    # by Initialize5_XXD. Output goes to standard output, so this function can be
    # used on its own as the dump generator or the preprocessor in DumpXXDInternal,
    # which is the internal dump function that's slow, but is supposed to be able
    # to generate all possible dumps.
    #

    Debug xxd "${1:--}"

    command -p xxd "${SCRIPT_XXD_OPTIONS[@]}" "${1:--}"
}

DumpXXDInternal() {
    local addr
    local addr_field_separator
    local addr_format
    local addr_prefix
    local addr_suffix
    local byte
    local byte_digits_per_octet
    local byte_digits_per_octet_xxd
    local byte_field_separator
    local byte_field_width
    local byte_field_width_xxd
    local byte_indent
    local byte_map
    local byte_output
    local byte_output_xxd
    local byte_padding
    local byte_prefix
    local byte_separator
    local byte_separator_size
    local byte_separator_size_xxd
    local byte_separator_xxd
    local byte_suffix
    local dump_record_length
    local dump_record_separator
    local line
    local opened
    local text
    local text_buffered
    local text_indent
    local text_map
    local text_output
    local text_prefix
    local text_separator
    local text_suffix

    #
    # Any dump that a user can request must be supported by this function, but
    # this function is only used when Initialize6_Handler decided there isn't a
    # a faster way to generate the dump. Everything we need from SCRIPT_STRINGS
    # is extracted and stored in a local variable and after that the while loop
    # processes one xxd output line at a time until there are no more lines.
    #

    dump_record_length="${SCRIPT_STRINGS[DUMP.record.length]}"
    dump_record_separator="${SCRIPT_STRINGS[DUMP.record.separator]}"

    addr_field_separator="${SCRIPT_STRINGS[ADDR.field.separator]}"
    addr_format="${SCRIPT_STRINGS[ADDR.format]}"
    addr_prefix="${SCRIPT_STRINGS[ADDR.prefix]}"
    addr_suffix="${SCRIPT_STRINGS[ADDR.suffix]}"

    byte_output="${SCRIPT_STRINGS[BYTE.output]}"
    byte_output_xxd="${SCRIPT_STRINGS[BYTE.output-xxd]}"
    byte_digits_per_octet="${SCRIPT_STRINGS[BYTE.digits.per.octet]}"
    byte_digits_per_octet_xxd="${SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]}"
    byte_field_separator="${SCRIPT_STRINGS[BYTE.field.separator]}"
    byte_field_width="${SCRIPT_STRINGS[BYTE.field.width]}"
    byte_field_width_xxd="${SCRIPT_STRINGS[BYTE.field.width.xxd]}"
    byte_indent="${SCRIPT_STRINGS[BYTE.indent]}"
    byte_map="${SCRIPT_STRINGS[BYTE.map]}"
    byte_prefix="${SCRIPT_STRINGS[BYTE.prefix]}"
    byte_separator="${SCRIPT_STRINGS[BYTE.separator]}"
    byte_separator_xxd="${SCRIPT_STRINGS[BYTE.separator-xxd]}"
    byte_separator_size="${SCRIPT_STRINGS[BYTE.separator.size]}"
    byte_separator_size_xxd="${SCRIPT_STRINGS[BYTE.separator.size.xxd]}"
    byte_suffix="${SCRIPT_STRINGS[BYTE.suffix]}"

    text_output="${SCRIPT_STRINGS[TEXT.output]}"
    text_indent="${SCRIPT_STRINGS[TEXT.indent]}"
    text_map="${SCRIPT_STRINGS[TEXT.map]}"
    text_prefix="${SCRIPT_STRINGS[TEXT.prefix]}"
    text_separator="${SCRIPT_STRINGS[TEXT.separator]}"
    text_suffix="${SCRIPT_STRINGS[TEXT.suffix]}"

    byte_padding=""
    opened=""
    text_buffered=""

    DumpXXD "${1:--}" | while true; do
        #
        # We're in a subshell that might be buffering the TEXT field, so we need
        # control over when we break out of the loop, otherwise all the buffered
        # text would be lost. Doing the read inside the subshell, rather than in
        # the pipeline, means we should be able to handle that potential problem.
        #
        if IFS="" read -r line; then
            #
            # The regular expression used to extract fields from xxd output lines
            # can match too many leading spaces in the TEXT field. It should only
            # happen on the last line of xxd output and is only an issue when we
            # want to use the ASCII output that xxd generated. It's easy to deal
            # with, and probably much easier than messing around with the regular
            # expression and then thoroughly testing the changes.
            #
            if [[ $line =~ ^(([[:xdigit:]]+):' ')?([[:xdigit:]]{$byte_digits_per_octet_xxd}(${byte_separator_xxd}[[:xdigit:]]{$byte_digits_per_octet_xxd})*)('  '(.*))?$ ]]; then
                addr="${BASH_REMATCH[2]}"
                byte="${BASH_REMATCH[3]}"
                text="${BASH_REMATCH[6]}"

                if (( byte_field_width > 0 )) && (( ${#byte} < byte_field_width_xxd)); then
                    #
                    # Before ANSI escape sequences were implemented none of this
                    # was needed. Each BYTE field in the dump, except perhaps the
                    # last one, contained same number of characters, and we could
                    # calculate that number during initialization and use it as a
                    # field width in the appropriate printf command. That approach
                    # doesn't always work now because escape sequences don't show
                    # up in the output, but they count as characters in the printf
                    # field width.
                    #
                    # Calculation multiplies the number of characters per octet
                    # in the dump's final output by the number of octets omitted
                    # (on the last line) of the xxd output. That number is used
                    # by the printf "%*s" format string as the field width of the
                    # string of spaces that's stored in byte_padding.
                    #
                    # It's an ugly calculation that's not worth spending much time
                    # on. To be honest, it often takes me a while to remember what
                    # I was trying to do here.
                    #
                    printf -v "byte_padding" "%*s" "$((
                        (byte_digits_per_octet + byte_separator_size)*(dump_record_length - (${#byte} + byte_separator_size_xxd)/(byte_digits_per_octet_xxd + byte_separator_size_xxd))
                    ))" ""
                else
                    byte_padding=""
                fi

                #
                # If there's an address in the xxd output make sure it's converted
                # from xxd's default (lowercase 8 digit hex field with leading 0s)
                # to the format the user really wants. This even works if the ADDR
                # field is supposed to be empty in the final dump, because in that
                # case ${addr_format} will be an empty string.
                #

                if [[ -n $addr ]]; then
                    #
                    # shellcheck disable=SC2059
                    #
                    printf -v "addr" "${addr_format}" "0x${addr}"       # xxd address is hex
                fi

                #
                # Next, make sure the TEXT field is what the user really wants. If
                # there's a TEXT field mapping array, use it and the xxd generated
                # BYTE field to build a new TEXT field. Otherwise, deal with a few
                # trivial outliers, like removing any extra leading spaces that the
                # regular expression may have included in the xxd TEXT field.
                #

                if [[ -n $text_map ]]; then
                    ByteMapper "$byte" "$byte_output_xxd" "$text_map" "text" "$text_separator"
                elif [[ $text_output == "ASCII" ]]; then
                    text="${text:byte_field_width_xxd - ${#byte}}"
                elif [[ $text_output == "EMPTY" ]]; then
                    text=""
                fi

                #
                # Finally, make sure the BYTE field is what the user really wants.
                # If there's a BYTE field mapping array, use it and the bytes that
                # xxd generated, to rebuild the BYTE field. Otherwise deal with a
                # few trivial outliers.
                #
                # NOTE - any changes to what's stored in the byte variable, which
                # can happen next, must follow all the code that assumes $byte was
                # generated by xxd.
                #

                if [[ -n $byte_map ]]; then
                    ByteMapper "$byte" "$byte_output_xxd" "$byte_map" "byte" "$byte_separator"
                elif [[ $byte_output == "EMPTY" ]]; then
                    byte=""
                elif [[ $byte_separator_xxd != "$byte_separator" ]]; then
                    byte="${byte//${byte_separator_xxd}/${byte_separator}}"
                fi

                #
                # At this point everything's ready, so see if $dump_record_length
                # is zero or not to decide what needs to be done.
                #

                if (( dump_record_length > 0 )); then
                    #
                    # This is the usual case. There's no buffering required and
                    # each line is printed immediately after it's generated by
                    # xxd. Values set during initialization mean a single fairly
                    # complicated printf call can handle each record, even when
                    # the layout is "NARROW".
                    #
                    # NOTE - the printf call is hard to read and probably could
                    # be simplified. The most important thing to notice are the
                    # bash expansions that look something like:
                    #
                    #     ${addr:+%s}
                    #
                    # They're called "Use Alternate Value" parameter expansions.
                    # Each one checks the named parameter (e.g., addr) and when
                    # it's empty nothing is substituted, otherwise what follows
                    # the ":+" token (e.g., %s) is (expanded) and substituted.
                    #
                    printf "${addr:+%s}${byte:+%s%s%s}${text:+%s}%s" \
                        ${addr:+"${addr_prefix}${addr}${addr_suffix}${addr_field_separator}"} \
                        ${byte:+"${byte_indent}${byte_prefix}"} ${byte:+"${byte}${byte_suffix}${byte_padding}"} ${byte:+"${byte_field_separator}"} \
                        ${text:+"${text_indent}${text_prefix}${text}${text_suffix}"} \
                        "${dump_record_separator}"
                else
                    #
                    # One address and all the bytes are supposed to print on a
                    # single line. After that, all the buffered text is either
                    # printed next to the BYTE field or directly below it.
                    #
                    if [[ $byte_output != "EMPTY" ]]; then
                        opened="BYTE"
                        printf "${addr:+%s}%s" \
                            ${addr:+"${addr_prefix}${addr}${addr_suffix}${addr_field_separator}"} \
                            "${byte_indent}${byte_prefix}${byte}"
                        text_buffered+="${text_buffered:+${text_separator}}${text}"
                        byte_indent=""
                        byte_prefix="$byte_separator"
                    elif [[ $text_output != "EMPTY" ]]; then
                        opened="TEXT"
                        printf "${addr:+%s}%s" \
                            ${addr:+"${addr_prefix}${addr}${addr_suffix}${addr_field_separator}"} \
                            "${text_indent}${text_prefix}${text}"
                        text_indent=""
                        text_prefix="$text_separator"
                    else
                        #
                        # Initialize is supposed to guarantee we never get here.
                        #
                        InternalError "byte and text fields can't both be EMPTY"
                    fi
                    addr_format=""              # omits rest of the addresses
                fi
            else
                InternalError "problem parsing xxd output line ${line@Q}"
            fi
        else
            #
            # Done reading input, but we're in a subshell, so any buffered text
            # has to be printed before we exit that subshell.
            #
            case "$opened" in
                "BYTE")
                    printf "%s" "${byte_suffix}"
                    if [[ -n $text_buffered ]]; then
                        printf "%s" "${byte_field_separator}${text_indent}${text_prefix}${text_buffered}${text_suffix}"
                    fi
                    printf "%s" "${dump_record_separator}";;

                "TEXT")
                    printf "%s" "${text_suffix}${dump_record_separator}";;
            esac
            break
        fi
    done
}

Help() {
    #
    # Used to show some documentation to the user. Right now that documentation
    # comes from special comments (near the end of this script) that start with
    # the string ${SCRIPT_STRINGS[SCRIPT.help.trigger]} (probably "#@#"). Using
    # comments has advantages, but it obviously assumes the script's source file,
    # as stored in ${BASH_SOURCE[0]}, can be accessed from the script's current
    # directory. Seems to be a reasonable assumption for this script, no matter
    # how it was originally found (e.g., via PATH or by using a relative path).
    # If not, it's something that should be pretty easy to address.
    #
    # NOTE - the HelpScanner function reads from standard input, so it doesn't
    # care where the documentation comes from. Storing it in a here document is
    # a very good alternative (as long as the delimiter word is quoted) that's
    # often used in bash scripts. Regular bash strings, whether they're single
    # or double quoted, have issues with individual characters that make them a
    # less convenient alternative.
    #

    if [[ -f ${BASH_SOURCE[0]} ]] && [[ -r ${BASH_SOURCE[0]} ]]; then
        HelpScanner -trigger="${SCRIPT_STRINGS[SCRIPT.help.trigger]}" +connected -copyright +license <"${BASH_SOURCE[0]}"
    elif [[ -n ${SCRIPT_STRINGS[SCRIPT.usage]} ]]; then
        printf "%s\n" "${SCRIPT_STRINGS[SCRIPT.usage]}"
    fi

    exit 0              # always quit
}

Initialize() {
    #
    # Handles the initialization that happens after all the command line options
    # are processed. The goal is to try to honor all the user's requests and to
    # finish as many calculations as we can outside the loop in DumpXXDInternal
    # that postprocesses xxd output. Some of those calculations are particularly
    # difficult, but almost all of them can be hard to read because they access
    # values in the SCRIPT_STRINGS associative array, which results in many long
    # and "noisy" bash expressions.
    #
    # All of the initialization could have been done right here in this function,
    # but there's so much code that splitting the work up into separate functions
    # seemed like a way to make it a little easier to follow. The names of those
    # functions were chosen so their (case independent) sorted order matched the
    # order that they're called. However, no matter how the initialization code
    # is organized, it's still the most complicated part of this script.
    #
    # NOTE - the good news is, if you're willing to believe this stuff works, you
    # probably can skip all the initialization, return to Main, and still follow
    # the rest of the script.
    #

    Initialize1_Begin           # must be called first

    #
    # The order of the function calls in the next group shouldn't be changed.
    #

    Initialize2_Fields
    Initialize3_FieldWidths
    Initialize4_Layout
    Initialize5_XXD
    Initialize6_Handler
    Initialize7_Maps
    Initialize8_Attributes

    #
    # At this point SCRIPT_STRINGS should be completely initialized.
    #

    Initialize9_End             # must be called last
}

Initialize1_Begin() {
    local attribute
    local -n attribute_array
    local field
    local index
    local layer

    #
    # We're completely finished with the command line options, so we can combine
    # the foreground and background attributes stored in separate arrays in the
    # the global SCRIPT_ATTRIBUTES array. Combining all of the attributes in one
    # array simplifies the code that eventually has to apply the attributes, but
    # also means we can set fields (BYTE.has.attributes and TEXT.has.attributes)
    # in SCRIPT_STRINGS that Initialize2_Fields uses to help decide if a mapping
    # array is needed.
    #

    for field in "BYTE" "TEXT"; do
        for layer in "BACKGROUND" "FOREGROUND"; do
            typeset -n attribute_array="SCRIPT_ATTRIBUTES_${field}_${layer}"
            for index in "${!attribute_array[@]}"; do
                attribute="${attribute_array[$index]}"
                if [[ -n $attribute ]]; then
                    if [[ -n ${SCRIPT_ANSI_ESCAPE[${attribute}]} ]]; then
                        #
                        # Add a "BYTE_" or "TEXT_" prefix to the attributes that
                        # are stored in SCRIPT_ATTRIBUTES. Means the field name
                        # and the attribute are encoded in each space separated
                        # string stored in SCRIPT_ATTRIBUTES. Decoding is done in
                        # Initialize8_Attributes.
                        #
                        SCRIPT_ATTRIBUTES[$index]+="${SCRIPT_ATTRIBUTES[$index]:+ }${field}_${attribute}"
                    fi
                fi
            done
        done
        #
        # shellcheck disable=SC2076
        #
        if [[ "${SCRIPT_ATTRIBUTES[*]}" =~ "${field}_" ]]; then
            SCRIPT_STRINGS[${field}.has.attributes]="TRUE"
        else
            SCRIPT_STRINGS[${field}.has.attributes]=""
        fi
    done
}

Initialize2_Fields() {
    #
    # The main job in this function is to check the output style that's currently
    # stored in SCRIPT_STRINGS for the ADDR, BYTE, and TEXT fields and use them to
    # initialize other fields that depend on the selected style. Nothing done here
    # involves difficult calculations and at this point if you knew how the fields
    # were used, primarily in DumpXXDInternal, most of this would be pretty easy.
    #
    # ADDR field initializations are first. It's easy to miss, but notice that the
    # value assigned to the ADDR.format key usually looks like it's a printf format
    # string - the EMPTY style is the one exception.
    #

    case "${SCRIPT_STRINGS[ADDR.output]}" in
        DECIMAL)
            SCRIPT_STRINGS[ADDR.format.width]="${SCRIPT_STRINGS[ADDR.format.width]:-${SCRIPT_STRINGS[ADDR.format.width.default]}}"
            SCRIPT_STRINGS[ADDR.format]="%${SCRIPT_STRINGS[ADDR.format.width]}d"
            SCRIPT_STRINGS[ADDR.digits]="${SCRIPT_STRINGS[ADDR.format.width]#0}";;

        EMPTY)
            SCRIPT_STRINGS[ADDR.format.width]="0"
            SCRIPT_STRINGS[ADDR.format.width-xxd]="0"
            SCRIPT_STRINGS[ADDR.format]=""
            SCRIPT_STRINGS[ADDR.format-xxd]=""
            SCRIPT_STRINGS[ADDR.digits]="0";;

        HEX-LOWER)
            SCRIPT_STRINGS[ADDR.format.width]="${SCRIPT_STRINGS[ADDR.format.width]:-${SCRIPT_STRINGS[ADDR.format.width.default]}}"
            SCRIPT_STRINGS[ADDR.format]="%${SCRIPT_STRINGS[ADDR.format.width]}x"
            SCRIPT_STRINGS[ADDR.digits]="${SCRIPT_STRINGS[ADDR.format.width]#0}";;

        HEX-UPPER)
            SCRIPT_STRINGS[ADDR.format.width]="${SCRIPT_STRINGS[ADDR.format.width]:-${SCRIPT_STRINGS[ADDR.format.width.default]}}"
            SCRIPT_STRINGS[ADDR.format]="%${SCRIPT_STRINGS[ADDR.format.width]}X"
            SCRIPT_STRINGS[ADDR.digits]="${SCRIPT_STRINGS[ADDR.format.width]#0}";;

        OCTAL)
            SCRIPT_STRINGS[ADDR.format.width]="${SCRIPT_STRINGS[ADDR.format.width]:-${SCRIPT_STRINGS[ADDR.format.width.default]}}"
            SCRIPT_STRINGS[ADDR.format]="%${SCRIPT_STRINGS[ADDR.format.width]}o"
            SCRIPT_STRINGS[ADDR.digits]="${SCRIPT_STRINGS[ADDR.format.width]#0}";;

        XXD)
            SCRIPT_STRINGS[ADDR.output]="HEX-LOWER"
            SCRIPT_STRINGS[ADDR.format.width]="${SCRIPT_STRINGS[ADDR.format.width]:-${SCRIPT_STRINGS[ADDR.format.width-xxd]}}"
            SCRIPT_STRINGS[ADDR.format]="%${SCRIPT_STRINGS[ADDR.format.width]}x"
            SCRIPT_STRINGS[ADDR.digits]="${SCRIPT_STRINGS[ADDR.format.width]#0}";;

         *) InternalError "address output ${SCRIPT_STRINGS[ADDR.output]@Q} has not been implemented";;
    esac

    if [[ ${SCRIPT_STRINGS[ADDR.output]} != "EMPTY" ]]; then
        if (( ${SCRIPT_STRINGS[ADDR.format.width.limit]} > 0 )); then
            #
            # This is a self-imposed limit. Edit SCRIPT_STRINGS to change or
            # disable the limit.
            #
            if (( ${SCRIPT_STRINGS[ADDR.format.width]#0} > ${SCRIPT_STRINGS[ADDR.format.width.limit]} )); then
                Error "address width ${SCRIPT_STRINGS[ADDR.format.width]#0} exceeds the limit of a ${SCRIPT_STRINGS[ADDR.format.width.limit]} digit address"
            fi
        fi
    fi

    SCRIPT_STRINGS[ADDR.prefix.size]="${#SCRIPT_STRINGS[ADDR.prefix]}"
    SCRIPT_STRINGS[ADDR.suffix.size]="${#SCRIPT_STRINGS[ADDR.suffix]}"
    SCRIPT_STRINGS[ADDR.field.separator.size]="${#SCRIPT_STRINGS[ADDR.field.separator]}"

    #
    # TEXT field initializations. Main job is to pick a TEXT field mapping array,
    # if it's needed.
    #

    case "${SCRIPT_STRINGS[TEXT.output]}" in
        ASCII)
            SCRIPT_STRINGS[TEXT.map]="${SCRIPT_STRINGS[TEXT.has.attributes]:+SCRIPT_ASCII_TEXT_MAP}"
            SCRIPT_STRINGS[TEXT.chars.per.octet]="1";;

        CARET)
            SCRIPT_STRINGS[TEXT.map]="SCRIPT_CARET_TEXT_MAP"
            SCRIPT_STRINGS[TEXT.chars.per.octet]="2";;

        CARET_ESCAPE)
            SCRIPT_STRINGS[TEXT.map]="SCRIPT_CARET_ESCAPE_TEXT_MAP"
            SCRIPT_STRINGS[TEXT.chars.per.octet]="2";;

        EMPTY)
            SCRIPT_STRINGS[TEXT.map]=""
            SCRIPT_STRINGS[TEXT.chars.per.octet]="0"
            SCRIPT_STRINGS[BYTE.field.separator]=""
            SCRIPT_STRINGS[BYTE.field.separator-xxd]=""
            SCRIPT_STRINGS[DUMP.layout]="${SCRIPT_STRINGS[DUMP.layout-xxd]}";;

        UNICODE)
            SCRIPT_STRINGS[TEXT.map]="SCRIPT_UNICODE_TEXT_MAP"
            SCRIPT_STRINGS[TEXT.chars.per.octet]="1";;

        XXD)
            SCRIPT_STRINGS[TEXT.output]="ASCII"
            SCRIPT_STRINGS[TEXT.map]="${SCRIPT_STRINGS[TEXT.has.attributes]:+SCRIPT_ASCII_TEXT_MAP}"
            SCRIPT_STRINGS[TEXT.chars.per.octet]="1";;

         *) InternalError "text output ${SCRIPT_STRINGS[TEXT.output]@Q} has not been implemented";;
    esac

    SCRIPT_STRINGS[TEXT.prefix.size]="${#SCRIPT_STRINGS[TEXT.prefix]}"
    SCRIPT_STRINGS[TEXT.separator.size]="${#SCRIPT_STRINGS[TEXT.separator]}"
    SCRIPT_STRINGS[TEXT.suffix.size]="${#SCRIPT_STRINGS[TEXT.suffix]}"

    #
    # Checking done next assumes that referenced TEXT field mapping arrays aren't
    # built by Initialize7_Maps, so they must already exist.
    #

    if [[ -n ${SCRIPT_STRINGS[TEXT.map]} ]]; then
        if [[ ${SCRIPT_STRINGS[TEXT.map]} =~ ^"SCRIPT_"[[:upper:]_]*"TEXT_MAP"$ ]]; then
            if [[ ! -v SCRIPT_STRINGS[TEXT.map] ]]; then
                InternalError "${SCRIPT_STRINGS[TEXT.map]@Q} is not an existing TEXT field mapping array"
            fi
        else
            InternalError "${SCRIPT_STRINGS[TEXT.map]@Q} is not recognized as a TEXT field mapping array name"
        fi
    fi

    #
    # BYTE field initializations. Main jobs are to select the output style that
    # want from xxd (i.e., BINARY, HEX-LOWER, or HEX-UPPER) and decide if we'll
    # need a BYTE field mapping array.
    #

    case "${SCRIPT_STRINGS[BYTE.output]}" in
        BINARY)
            SCRIPT_STRINGS[BYTE.output-xxd]="BINARY"
            SCRIPT_STRINGS[BYTE.map]="${SCRIPT_STRINGS[BYTE.has.attributes]:+SCRIPT_BYTE_MAP}"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="8"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="8";;

        DECIMAL)
            SCRIPT_STRINGS[BYTE.output-xxd]="HEX-LOWER"
            SCRIPT_STRINGS[BYTE.map]="SCRIPT_BYTE_MAP"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="3"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2";;

        EMPTY)
            if [[ ${SCRIPT_STRINGS[TEXT.output]} != "EMPTY" ]]; then
                SCRIPT_STRINGS[BYTE.output-xxd]="HEX-LOWER"
                SCRIPT_STRINGS[BYTE.map]=""
                SCRIPT_STRINGS[BYTE.digits.per.octet]="0"
                SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2"
                SCRIPT_STRINGS[DUMP.layout]="${SCRIPT_STRINGS[DUMP.layout-xxd]}"
            else
                Error "byte and text fields can't both be empty"
            fi;;

        HEX-LOWER)
            SCRIPT_STRINGS[BYTE.output-xxd]="HEX-LOWER"
            SCRIPT_STRINGS[BYTE.map]="${SCRIPT_STRINGS[BYTE.has.attributes]:+SCRIPT_BYTE_MAP}"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="2"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2";;

        HEX-UPPER)
            SCRIPT_STRINGS[BYTE.output-xxd]="HEX-UPPER"
            SCRIPT_STRINGS[BYTE.map]="${SCRIPT_STRINGS[BYTE.has.attributes]:+SCRIPT_BYTE_MAP}"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="2"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2";;

        OCTAL)
            SCRIPT_STRINGS[BYTE.output-xxd]="HEX-LOWER"
            SCRIPT_STRINGS[BYTE.map]="SCRIPT_BYTE_MAP"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="3"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2";;

        XXD)
            SCRIPT_STRINGS[BYTE.output]="HEX-LOWER"
            SCRIPT_STRINGS[BYTE.output-xxd]="HEX-LOWER"
            SCRIPT_STRINGS[BYTE.map]="${SCRIPT_STRINGS[BYTE.has.attributes]:+SCRIPT_BYTE_MAP}"
            SCRIPT_STRINGS[BYTE.digits.per.octet]="2"
            SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]="2";;

         *) InternalError "byte output ${SCRIPT_STRINGS[BYTE.output]@Q} has not been implemented";;
    esac

    #
    # Checking done next assumes there's only one valid BYTE field mapping array
    # name (i.e., SCRIPT_BYTE_MAP), which at this point is an empty array. If that
    # array is referenced by SCRIPT_STRINGS[BYTE.map], then Initialize7_Maps makes
    # sure it's eventually built.
    #

    if [[ -n ${SCRIPT_STRINGS[BYTE.map]} ]]; then
        if [[ ! ${SCRIPT_STRINGS[BYTE.map]} =~ ^"SCRIPT_BYTE_MAP"$ ]]; then
            InternalError "${SCRIPT_STRINGS[BYTE.map]@Q} is not recognized as a BYTE field mapping array name"
        fi
    fi

    if (( ${SCRIPT_STRINGS[DUMP.record.length.limit]} > 0 )); then
        #
        # There's an upper limit for xxd but apparently not for od, so we could
        # get around this limit by strategically switching to od to generate the
        # dump if we've exceeded xxd's limit. In that case, something like
        #
        #     od --output-duplicates --address-radix=x --width=257 --format=x1z
        #
        # followed by some simple sed cleanup can sometimes make od output look
        # like it came from xxd. Absolutely no way to justify the required work
        # in a bash script. The way to address the xxd limit is to rewrite the
        # entire bash script in a language with better data structures that can
        # also deal with every byte (bash strings can't store null bytes).
        #
        if (( ${SCRIPT_STRINGS[DUMP.record.length]} > ${SCRIPT_STRINGS[DUMP.record.length.limit]} )); then
            Error "record length ${SCRIPT_STRINGS[DUMP.record.length]} exceeds the limit of ${SCRIPT_STRINGS[DUMP.record.length.limit]} bytes per record"
        fi
    fi

    SCRIPT_STRINGS[BYTE.prefix.size]="${#SCRIPT_STRINGS[BYTE.prefix]}"
    SCRIPT_STRINGS[BYTE.separator.size]="${#SCRIPT_STRINGS[BYTE.separator]}"
    SCRIPT_STRINGS[BYTE.suffix.size]="${#SCRIPT_STRINGS[BYTE.suffix]}"
    SCRIPT_STRINGS[BYTE.field.separator.size]="${#SCRIPT_STRINGS[BYTE.field.separator]}"

    if [[ -z ${SCRIPT_STRINGS[BYTE.separator]} ]]; then
        SCRIPT_STRINGS[BYTE.grouping.xxd]="0"
        SCRIPT_STRINGS[BYTE.separator-xxd]=""
        SCRIPT_STRINGS[BYTE.separator.size.xxd]="${#SCRIPT_STRINGS[BYTE.separator-xxd]}"
    else
        SCRIPT_STRINGS[BYTE.grouping.xxd]="1"
        SCRIPT_STRINGS[BYTE.separator-xxd]=" "
        SCRIPT_STRINGS[BYTE.separator.size.xxd]="${#SCRIPT_STRINGS[BYTE.separator-xxd]}"
    fi
}

Initialize3_FieldWidths() {
    #
    # The internal dump function needs to know the full width (in characters not
    # bytes) of the BYTE fields produced by xxd and requested by the user. That
    # number is calculated here, stored in SCRIPT_STRINGS[BYTE.field.width.xxd],
    # and used to manage the space padding of the BYTE and TEXT fields, usually
    # only on xxd's last line of output.
    #
    # NOTE - before ANSI color escapes were implemented BYTE.field.width was used
    # as a field width in a printf conversion specification. That approach broke
    # after ANSI escapes were implemented, so now BYTE.field.width is only used
    # when DumpXXDInternal needs to generate a "padding" string.
    #

    if (( ${SCRIPT_STRINGS[DUMP.record.length]} > 0 )); then
        SCRIPT_STRINGS[DUMP.record.length-xxd]="${SCRIPT_STRINGS[DUMP.record.length]}"
        if [[ ${SCRIPT_STRINGS[BYTE.output]} == "EMPTY" || ${SCRIPT_STRINGS[TEXT.output]} == "EMPTY" || ${SCRIPT_STRINGS[DUMP.layout]} == "NARROW" ]]; then
            #
            # Each collection of bytes that make up a BYTE field should be the
            # last thing on a line, so setting the field width to 0 guarantees
            # that printf won't pad any of them on the right with spaces.
            #
            SCRIPT_STRINGS[BYTE.field.width]="0"
        else
            #
            # Otherwise try to accurately calculate the BYTE field width in the
            # dump we generate, but ANSI escapes mean all we really use it for
            # is to test whether it's 0 or nonzero. Basically, a nonzero value
            # means the BYTE field won't be the last thing on a line, so the
            # internal dump function may need to generate a padding string to
            # make sure everything lines up properly.
            #
            SCRIPT_STRINGS[BYTE.field.width]=$((
                ${SCRIPT_STRINGS[DUMP.record.length]}*(${SCRIPT_STRINGS[BYTE.digits.per.octet]} + ${SCRIPT_STRINGS[BYTE.separator.size]}) -
                ${SCRIPT_STRINGS[BYTE.separator.size]}
            ))
        fi
    else
        #
        # We treat a 0 record length as a request that all the bytes be printed
        # together in a single BYTE field and on one line.
        #
        # Using the largest number of bytes that xxd can output per line means
        # the internal dump function makes the smallest number of trips through
        # its outer loop. Not timed, but it seems like a reasonable guess about
        # the best way to run this script.
        #
        SCRIPT_STRINGS[DUMP.record.length-xxd]="${SCRIPT_STRINGS[DUMP.record.length.limit.xxd]}"
        SCRIPT_STRINGS[BYTE.field.width]="0"
    fi

    SCRIPT_STRINGS[BYTE.field.width.xxd]=$((
        ${SCRIPT_STRINGS[DUMP.record.length-xxd]}*(${SCRIPT_STRINGS[BYTE.digits.per.octet-xxd]} + ${SCRIPT_STRINGS[BYTE.grouping.xxd]}) -
        ${SCRIPT_STRINGS[BYTE.grouping.xxd]}
    ))
}

Initialize4_Layout() {
    local padding

    #
    # Different "layouts" give the user a little control over the arrangement of
    # each record's ADDR, BYTE, and TEXT fields. The currently supported layouts
    # are named "WIDE" and "NARROW". There are other possibilites, but these two
    # are easy to describe and feel like they should be more than sufficent. They
    # can be requested on the command line using the --wide or --narrow options.
    #
    # WIDE is the default layout and it generally resembles the way xxd organizes
    # its output - the ADDR, BYTE, and TEXT fields are printed next to each other,
    # and in that order, on the same line.
    #
    # The NARROW layout is harder and needs lots of subtle calculations to make
    # sure everything gets lined up properly. Basically NARROW layout prints the
    # ADDR and BYTE fields next to each other on the same line. The TEXT field is
    # printed by itself on the next line and what we have to do here is make sure
    # each character in the TEXT field is printed directly below the appropriate
    # byte in the BYTE field. The calculations are painful, particularly in bash,
    # so they've been split into small steps that hopefully will be little easier
    # to follow.
    #
    # NOTE - values adjusted here are used by DumpXXDInternal, so that's where to
    # look if you really want to understand what we're trying to accomplish here.
    #

    if [[ ${SCRIPT_STRINGS[DUMP.layout]} == "NARROW" ]]; then
        #
        # In this case, each TEXT field is supposed to be printed directly below
        # the corresponding BYTE field. Lots of adjustments will be required, but
        # the first step is make sure the TEXT and BYTE fields print on separate
        # lines by assigning a newline to the string that separates the BYTE field
        # from the TEXT field.
        #

        SCRIPT_STRINGS[BYTE.field.separator]=$'\n'
        SCRIPT_STRINGS[BYTE.field.separator.size]="${#SCRIPT_STRINGS[BYTE.field.separator]}"

        #
        # Figure out the number of spaces that need to be appended to the TEXT or
        # BYTE field indents to make the first byte and first character end up in
        # the same "column". The positioning of individual text characters within
        # the column will be addressed separately.
        #

        padding=$((${SCRIPT_STRINGS[BYTE.prefix.size]} - ${SCRIPT_STRINGS[TEXT.prefix.size]}))
        if (( padding > 0 )); then
            SCRIPT_STRINGS[TEXT.indent]+="$(printf "%*s" "$padding" "")"
        elif (( padding < 0 )); then
            SCRIPT_STRINGS[BYTE.indent]+="$(printf "%*s" "$((-padding))" "")"
        fi

        #
        # Next, modify the separation between individual bytes in the BYTE field
        # or characters in the TEXT field so they all can be lined up vertically
        # when they're printed on separate lines.
        #

        padding=$((${SCRIPT_STRINGS[BYTE.digits.per.octet]} - ${SCRIPT_STRINGS[TEXT.chars.per.octet]} + ${SCRIPT_STRINGS[BYTE.separator.size]} - ${SCRIPT_STRINGS[TEXT.separator.size]}))
        if (( padding > 0 )); then
            SCRIPT_STRINGS[TEXT.separator]+="$(printf "%*s" "$padding" "")"
            SCRIPT_STRINGS[TEXT.separator.size]="${#SCRIPT_STRINGS[TEXT.separator]}"
            if [[ ${SCRIPT_STRINGS[TEXT.output]} == "ASCII" ]] && [[ -z ${SCRIPT_STRINGS[TEXT.map]} ]]; then
                #
                # Separation changed and we're doing ASCII output, so make sure
                # every ASCII TEXT field is regenerated using the new separation.
                #
                SCRIPT_STRINGS[TEXT.map]="SCRIPT_ASCII_TEXT_MAP"
            fi
        elif (( padding < 0 )); then
            SCRIPT_STRINGS[BYTE.separator]+="$(printf "%*s" "$((-padding))" "")"
            SCRIPT_STRINGS[BYTE.separator.size]="${#SCRIPT_STRINGS[BYTE.separator]}"
        fi

        #
        # Adjust the TEXT field prefix by appending the number of spaces needed
        # to make sure the first character lines up right adjusted and directly
        # below the first displayed byte.
        #

        padding=$((${SCRIPT_STRINGS[BYTE.digits.per.octet]} - ${SCRIPT_STRINGS[TEXT.chars.per.octet]}))
        if (( padding > 0 )); then
            SCRIPT_STRINGS[TEXT.prefix]+="$(printf "%*s" "$padding" "")"
        elif (( padding < 0 )); then
            InternalError "chars per octet exceeds digits per octet"
        fi

        #
        # If there's an address, adjust the TEXT field indent so all characters
        # line up vertically with the appropriate BYTE field bytes.
        #

        if [[ ${SCRIPT_STRINGS[ADDR.output]} != "EMPTY" ]]; then
            padding=$((${SCRIPT_STRINGS[ADDR.prefix.size]} + ${SCRIPT_STRINGS[ADDR.digits]} + ${SCRIPT_STRINGS[ADDR.suffix.size]} + ${SCRIPT_STRINGS[ADDR.field.separator.size]}))
            if (( padding > 0 )); then
                SCRIPT_STRINGS[TEXT.indent]+="$(printf "%*s" "$padding" "")"
            fi
        fi
    elif [[ ${SCRIPT_STRINGS[DUMP.layout]} != "WIDE" ]]; then
        InternalError "layout ${SCRIPT_STRINGS[DUMP.layout]@Q} has not been implemented"
    fi
}

Initialize5_XXD() {
    #
    # Examines some of the values currently stored in SCRIPT_STRINGS and uses them
    # to figure out the command line options that need to be used when we run xxd.
    # All of the xxd options are stored in the global SCRIPT_XXD_OPTIONS array.
    #
    # NOTE - this function occasionally updates some xxd fields in SCRIPT_STRINGS,
    # so it has to be called before Initialize6_Handler.
    #

    SCRIPT_XXD_OPTIONS=()

    SCRIPT_XXD_OPTIONS+=("-c" "${SCRIPT_STRINGS[DUMP.record.length-xxd]}")
    SCRIPT_XXD_OPTIONS+=("-g" "${SCRIPT_STRINGS[BYTE.grouping.xxd]}")

    if (( ${SCRIPT_STRINGS[DUMP.input.count]} > 0 )); then
        SCRIPT_XXD_OPTIONS+=("-l" "${SCRIPT_STRINGS[DUMP.input.count]}")
    fi

    if (( ${SCRIPT_STRINGS[DUMP.input.start]} > 0 )); then
        SCRIPT_XXD_OPTIONS+=("-s" "${SCRIPT_STRINGS[DUMP.input.start]}")
    fi

    if (( ${SCRIPT_STRINGS[DUMP.output.start]} != ${SCRIPT_STRINGS[DUMP.input.start]} )); then
        SCRIPT_XXD_OPTIONS+=("-o" "$((${SCRIPT_STRINGS[DUMP.output.start]} - ${SCRIPT_STRINGS[DUMP.input.start]}))")
    fi

    if [[ ${SCRIPT_STRINGS[BYTE.output]} == "HEX-UPPER" ]]; then
        SCRIPT_XXD_OPTIONS+=("-u")
    elif [[ ${SCRIPT_STRINGS[BYTE.output]} == "BINARY" ]]; then
        SCRIPT_XXD_OPTIONS+=("-bits")
    fi

    #
    # See if xxd's postscript style output is a good fit for our dump, and if it
    # is, add the "-ps" option to SCRIPT_XXD_OPTIONS.
    #

    if [[ ${SCRIPT_STRINGS[ADDR.output]} == "EMPTY" && ${SCRIPT_STRINGS[TEXT.output]} == "EMPTY" ]]; then
        if [[ ${SCRIPT_STRINGS[BYTE.output]} =~ ^("HEX-LOWER"|"HEX-UPPER")$ ]]; then
            #
            # Should work if there's no separation between the bytes or output is
            # just one column.
            #
            if (( ${SCRIPT_STRINGS[BYTE.grouping.xxd]} == 0 )) || (( ${SCRIPT_STRINGS[BYTE.grouping.xxd]} == 1 && ${SCRIPT_STRINGS[DUMP.record.length]} == 1 )); then
                SCRIPT_XXD_OPTIONS+=("-ps")
                #
                # The next two lines update the description of the bytedump that
                # xxd generates when it's handed the "-ps" option. They tell the
                # Initialize6_Handler function the raw xxd output won't include
                # ADDR or TEXT fields, which helps it do a better job deciding if
                # xxd output matches the dump the user has requested.
                #
                SCRIPT_STRINGS[ADDR.output-xxd]="EMPTY"
                SCRIPT_STRINGS[TEXT.output-xxd]="EMPTY"
            fi
        fi
    fi
}

Initialize6_Handler() {
    local checked
    local failed
    local key
    local key_xxd
    local skipped

    #
    # Looks through the current settings in the SCRIPT_STRINGS associative array
    # and tries to pick the "fastest" way to generate the dump described in that
    # that array. Our answer is saved in SCRIPT_STRINGS[DUMP.handler] and it's a
    # string that's the "internal name" associated with the shell function that's
    # supposed to generate the dump that the user has requested. The two choices
    # for that name currently are "DUMP-XXD" or "DUMP-INTERNAL".
    #

    SCRIPT_STRINGS[DUMP.handler]="DUMP-XXD"

    checked=""
    failed=""
    skipped=""

    for key_xxd in "${!SCRIPT_STRINGS[@]}"; do
        #
        # Only check official looking keys that end in the "-xxd" suffix.
        #
        if [[ $key_xxd =~ ^([[:upper:]]+([.][[:lower:]]+)+)"-xxd"$ ]]; then
            #
            # The associated key is the one we get by removing the "-xxd" suffix.
            #
            key="${BASH_REMATCH[1]}"
            checked+="${checked:+ }${key_xxd}"
            if [[ -v SCRIPT_STRINGS[${key}] ]]; then
                #
                # If the two keys don't match then xxd can't produce the final
                # dump, so we have to call DumpXXDInternal to do the work.
                #
                if [[ ${SCRIPT_STRINGS[$key_xxd]} != "${SCRIPT_STRINGS[${key}]}" ]]; then
                    SCRIPT_STRINGS[DUMP.handler]="DUMP-INTERNAL"
                    failed+="${failed:+ }${key_xxd}"
                    #
                    # Recording all the failures, rather than just the first one,
                    # might occasionally be useful debugging information.
                    #
                    if [[ ${SCRIPT_STRINGS[DEBUG.handler]} == "FALSE" ]]; then
                        break
                    fi
                fi
            else
                skipped+="${skipped:+ }${key_xxd}"
            fi
        fi
    done

    SCRIPT_STRINGS[INFO.handler.keys.checked]="$checked"
    SCRIPT_STRINGS[INFO.handler.keys.failed]="$failed"
    SCRIPT_STRINGS[INFO.handler.keys.skipped]="$skipped"
}

Initialize7_Maps() {
    local -n field_map
    local index
    local unexpanded

    #
    # Makes sure all required mapping arrays exist and are properly initialized.
    # Only builds BYTE field mapping arrays, so a referenced TEXT field mapping
    # array must already exist. The TEXT field mapping array is checked, just to
    # make sure that all Unicode escape sequences in its initializer were really
    # expanded. After that, the ANSI escape sequences the user referenced using
    # foreground and background command line options are added to the selected
    # entries in the mapping arrays.
    #

    if [[ -n ${SCRIPT_STRINGS[BYTE.map]} ]]; then
        if [[ ${SCRIPT_STRINGS[BYTE.map]} == "SCRIPT_BYTE_MAP" ]]; then
            #
            # Using a nameref variable here is overkill because there's only one
            # allowed name, and that probably won't ever change.
            #

            typeset -n field_map="${SCRIPT_STRINGS[BYTE.map]}"

            #
            # There's an explicit assumption made about the order of the elements
            # in the arrays that we build using bash's brace expansion. If you're
            # not convinced there are longer, but much less obscure ways to build
            # the BYTE field mapping arrays.
            #
            case "${SCRIPT_STRINGS[BYTE.output]}" in
                BINARY)
                    field_map=({0,1}{0,1}{0,1}{0,1}{0,1}{0,1}{0,1}{0,1});;

                DECIMAL)
                    field_map=('  '{0..9} ' '{1..9}{0..9} 1{0..9}{0..9} 2{0..4}{0..9} 25{0..5});;

                HEX-LOWER)
                    field_map=({0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f}{0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f});;

                HEX-UPPER)
                    field_map=({0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F}{0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F});;

                OCTAL)
                    field_map=({0..3}{0..7}{0..7});;

                 *) InternalError "builder for base ${SCRIPT_STRINGS[BYTE.output]@Q} map has not been implemented";;
            esac
        else
            InternalError "${SCRIPT_STRINGS[BYTE.map]@Q} is not a recognized byte mapping array name"
        fi
    fi

    #
    # We never build TEXT field mapping arrays, so every one that's referenced
    # must be hardcoded. This is also where the script adjust the Unicode escape
    # sequences in the selected TEXT field mapping array's initializer that bash
    # wasn't able to expand (see "Locales And Encoding" for more details).
    #

    if [[ -n ${SCRIPT_STRINGS[TEXT.map]} ]]; then
        #
        # Checking for existence using -v won't catch every programming mistake,
        # but it is sufficient for our purposes.
        #
        if [[ -v ${SCRIPT_STRINGS[TEXT.map]} ]]; then
            #
            # shellcheck disable=SC2178
            #
            typeset -n field_map="${SCRIPT_STRINGS[TEXT.map]}"

            #
            # As discussed in the "Locales And Encoding" block of comments, Unicode
            # escape sequences in TEXT field mappng array initializers that can't be
            # expanded (in the user's locale) are replaced by bash with an equivalent
            # escape sequence that uses four hex digits. For example, if bash found
            #
            #     $'^\uC2'
            #
            # in an array initializer but wasn't able to expand the escape sequence,
            # it would quietly replace the two hex digits with four hex digits that
            # represent the same number and the string (without quotes)
            #
            #     '^\u00C2'
            #
            # would end up in the TEXT field mapping array. This is where we try to
            # clean things up, basically by replacing any unexpanded elements in the
            # TEXT field mapping array with a string that's just question marks.
            #
            # NOTE - the code is a little tricky, but the idea is pretty simple. If
            # you're using Linux you should be able to run this script and there are
            # two debugging options that will let you see what's happening. The first
            # command to run would be something like
            #
            #     LC_ALL=C ./bytedump --text=caret --debug=textmap,unexpanded /dev/null
            #
            # which dumps the text mapping array, exactly the way it was initially
            # built by bash. Adding "unexpanded" as one of the debug options makes
            # sure the cleanup code that follows these comments doesn't run, and
            # that gives you a good look at what needs to be fixed. After that run
            #
            #     LC_ALL=C ./bytedump --text=caret --debug=textmap /dev/null
            #
            # which drops "unexpanded" from the debug options that were used in the
            # first command, and you'll see what the cleanup code does.
            #

            if [[ ${SCRIPT_STRINGS[DEBUG.unexpanded]} == "FALSE" ]]; then
                #
                # If we're going to look for sequences of characters that bash left in
                # the TEXT field mapping array when it had trouble expanding a Unicode
                # escape sequence, then we should do it in the user's locale. It's also
                # needed because we want to count characters (not bytes) to figure out
                # how many question marks to use when those mistakes are replaced.
                #

                LC_ALL="${SCRIPT_LC_ALL[EXTERNAL]}"

                #
                # Look for an unexpanded Unicode escape sequence in the mapping array.
                #
                if [[ "${field_map[*]}" =~ '\u'[[:xdigit:]]{4} ]]; then
                    unexpanded="${SCRIPT_STRINGS[DUMP.unexpanded.char]:-"?"}"

                    #
                    # There's at least one, so check every element.
                    #
                    for (( index = 0; index < 256; index++ )); do
                        #
                        # Only try to fix one unexpanded Unicode escape sequence at the
                        # end of each string in the TEXT field mapping array. Sufficient
                        # for our purposes, but it's not completely general.
                        #
                        if [[ ${field_map[$index]} =~ ^(.*)('\u'[[:xdigit:]]{4})$ ]]; then
                            #
                            # Found one, so replace it with a string that's filled with the
                            # right number of question marks. We build it using printf, a
                            # curious looking format string, and the seq command to create
                            # the required number of "dummy" arguments.
                            #

                            #
                            # shellcheck disable=SC2046
                            #
                            printf -v "field_map[$index]" "%.0s${unexpanded:0:1}" $(command -p seq 0 ${#BASH_REMATCH[1]})
                        fi
                    done
                fi

                #
                # Back to the script's preferred locale.
                #

                LC_ALL="${SCRIPT_LC_ALL[INTERNAL]}"
            fi
        else
            InternalError "${SCRIPT_STRINGS[TEXT.map]@Q} isn't the name of a TEXT field mapping array"
        fi
    fi
}

Initialize8_Attributes() {
    local attribute
    local attribute_name
    local escape_prefix
    local escape_suffix
    local -n field_map
    local field_name
    local index

    #
    # This is where the "attributes" that were stored in SCRIPT_ATTRIBUTES by the
    # Initialize1_Begin function are "decoded" and applied to individual elements
    # in the TEXT or BYTE field mapping arrays. It's confusing code, but my first
    # try was much worse - my advice is just skip this stuff.
    #
    # NOTE - three undocumented debugging options might help if you're trying to
    # make sense out this stuff. The command lines
    #
    #     ./bytedump --debug=attributes,bytemap,textmap --text-foreground='red: [:digit:]' /dev/null
    #
    # or
    #
    #     ./bytedump --debug=attributes,bytemap,textmap --foreground='red: [:digit:]' /dev/null
    #
    # dump the SCRIPT_ATTRIBUTES array and the final contents of the TEXT and BYTE
    # field mapping arrays, which should also include the colors that you selected
    # using command line options.
    #

    if (( ${#SCRIPT_ATTRIBUTES[@]} > 0 )); then
        escape_suffix="${SCRIPT_ANSI_ESCAPE[RESET.attributes]}"
        #
        # Loop through the indices currently defined in SCRIPT_ATTRIBUTES. Using
        # the --debug=attributes command line option will show you what's stored
        # in that array.
        #
        for index in "${!SCRIPT_ATTRIBUTES[@]}"; do
            for attribute in ${SCRIPT_ATTRIBUTES[$index]}; do
                for field_name in "BYTE" "TEXT"; do
                    if [[ -n ${SCRIPT_STRINGS[${field_name}.map]} ]]; then
                        #
                        # Extract the "key" we have to use to find the ANSI escape
                        # sequence in SCRIPT_ANSI_ESCAPE, basically by omitting the
                        # field name specific prefix that Initialize1_Begin added.
                        #
                        if [[ $attribute =~ ^(${field_name}_)((BACKGROUND|FOREGROUND)[.][[:alnum:]-]+)$ ]]; then
                            attribute_name="${BASH_REMATCH[2]}"
                            escape_prefix=""
                            if [[ -n ${SCRIPT_ANSI_ESCAPE[$attribute_name]} ]]; then
                                escape_prefix="${SCRIPT_ANSI_ESCAPE[$attribute_name]}"
                                if [[ -n $escape_prefix ]]; then
                                    #
                                    # shellcheck disable=SC2178
                                    #
                                    typeset -n field_map="${SCRIPT_STRINGS[${field_name}.map]}"

                                    field_map[$index]="${escape_prefix}${field_map[$index]}${escape_suffix}"
                                fi
                            fi
                        fi
                    fi
                done
            done
        done
    fi
}

Initialize9_End() {
    #
    # Everything in SCRIPT_STRINGS should be initialized and most of it has been
    # checked, including the BYTE and TEXT field mapping arrays. There shouldn't
    # be any more changes to values stored in the SCRIPT_STRINGS array, so make
    # it readonly - even though nothing really depends on that protection.
    #
    # After that, make sure nonempty strings assigned to the BYTE.map and TEXT.map
    # keys in SCRIPT_STRINGS actually reference existing variables. Checking here,
    # after SCRIPT_STRINGS is locked down, should convince you that the ByteMapper
    # function won't have to check everytime it's called.
    #

    readonly SCRIPT_STRINGS

    if [[ -n ${SCRIPT_STRINGS[BYTE.map]} ]]; then
        if [[ ! -v ${SCRIPT_STRINGS[BYTE.map]} ]]; then
            InternalError "${SCRIPT_STRINGS[BYTE.map]@Q} isn't the name of an existing BYTE field mapping array"
        fi
    fi

    if [[ -n ${SCRIPT_STRINGS[TEXT.map]} ]]; then
        if [[ ! -v ${SCRIPT_STRINGS[TEXT.map]} ]]; then
            InternalError "${SCRIPT_STRINGS[TEXT.map]@Q} isn't the name of an existing TEXT field mapping array"
        fi
    fi
}

Main() {
    #
    # This function runs the script, basically by just calling the functions that
    # do the real work. The only tricky part here is skipping over of the command
    # line arguments that were consumed as options. We're able to shift them out
    # of the picture because Options sets SCRIPT_ARGUMENTS_CONSUMED to the number
    # of arguments that it consumed, right before it returns.
    #
    # NOTE - the work orchestrated by Initialize probably is the hardest stuff to
    # follow in this script. There's lots of code, a bunch ugly bash arithmetic
    # that often involves long variable "names" and tricky parameter expansions,
    # and the result is usually stashed away in the SCRIPT_STRINGS array without
    # any indication how (or when) those values will be used. If you're willing
    # to believe the initialization works, you probably can just skip Initialize,
    # and there's a good chance you'll be able to follow the rest of the script.
    #

    Setup
    Options "$@"
    shift "$((SCRIPT_ARGUMENTS_CONSUMED))"      # skip to first non-option argument
    Initialize                                  # stuff done here is a tough read
    Debug
    Arguments "$@"

    Debug locals                                # check for "leaked locals"
}

Options() {
    local arg
    local argc
    local attribute
    local length
    local optarg
    local regex_number
    local regex_separator
    local selector
    local style
    local target
    local width

    #
    # A simple option handler that doesn't use the bash getopts builtin or depend
    # on user commands, like getopt. The caller needs to know how many arguments
    # were consumed as options. That number is calculated, by counting the number
    # arguments when the function starts and right before it returns, and storing
    # the difference in the global SCRIPT_ARGUMENTS_CONSUMED variable.
    #
    # This is a long function, but its length has nothing to do with fact that it
    # doesn't rely on getopts (or the getopt command) to help process command line
    # options. It's long because there are lots of options and most of them expect
    # arguments that must be validated and parsed, which isn't a job that getopts
    # or getopt handle. Take a look at
    #
    #     https://mywiki.wooledge.org/BashFAQ/035
    #
    # if you want more information about option handling in bash scripts.
    #
    # NOTE - there are a few variables that look like they could be declared as
    # integers (e.g., length, width). However, in those cases we need to be able
    # to detect when a null string has been assigned to them, and that wouldn't
    # be possible if they were integer variables - bash automatically turns null
    # strings assigned to integer variables into zeros. You may also notice that
    # we force their conversion to decimal, using expressions like
    #
    #     SCRIPT_STRINGS[DUMP.record.length]=$((length))
    #
    # so the numeric values, stored as strings in the SCRIPT_STRINGS array, don't
    # need to be converted from octal or hex to decimal.
    #
    # TODO - use (and avoidance) of character classes in this function is kind of
    # sloppy and probably should be revisited. It's currently safe to assume that
    # the C locale is being used when this function is called and that's probably
    # the way it will always be. Haven't found the time to clean things up, so I
    # apologize for any confusion.
    #

    argc="$#"

    #
    # A few definitions of simple regular expression definitions that are used to
    # parse option arguments. Decided not to use the [:digit:] character class or
    # ranges, like [0-9], in the regex_number definition, even though either one
    # should work because at this point we're supposed to be using the C locale.
    #
    # NOTE - don't use parentheses for grouping in any of these definitions. The
    # code that uses them assumes bash's BASH_REMATCH array is filled with tokens
    # that are extracted from an option's argument after bash matches the regular
    # expression that's used to validate the argument. Parentheses that are used
    # in these definitions will break some of the indexing into BASH_REMATCH.
    #

    regex_number='[123456789][0123456789]*|0[xX][[:xdigit:]]+|0[01234567]*'
    regex_separator='[[:blank:]]*[:][[:blank:]]*'

    while (( $# > 0 )); do
        arg="$1"
        if [[ $arg =~ ^--([^=]+)[=](.+)$ ]]; then
            optarg="${BASH_REMATCH[2]}"
        else
            optarg=""
        fi
        case "$arg" in
            --addr=?*)
                if [[ $optarg =~ ^(decimal|empty|hex|HEX|octal|xxd)(${regex_separator}([0]?[123456789][0123456789]*))?$ ]]; then
                    style="${BASH_REMATCH[1]}"
                    width="${BASH_REMATCH[3]}"
                    case "$style" in
                        decimal) SCRIPT_STRINGS[ADDR.output]="DECIMAL";;
                          empty) SCRIPT_STRINGS[ADDR.output]="EMPTY";;
                            hex) SCRIPT_STRINGS[ADDR.output]="HEX-LOWER";;
                            HEX) SCRIPT_STRINGS[ADDR.output]="HEX-UPPER";;
                          octal) SCRIPT_STRINGS[ADDR.output]="OCTAL";;
                            xxd) SCRIPT_STRINGS[ADDR.output]="XXD";;
                              *) InternalError "option ${arg@Q} has not been completely implemented";;
                    esac
                    if [[ -n $width ]]; then
                        #
                        # The way the argument is currently parsed means we can't use
                        # arithmetic expansion here because a leading 0 is meaningful.
                        # It will eventually be used as a flag character in a printf
                        # format specification.
                        #
                        SCRIPT_STRINGS[ADDR.format.width]="$width"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --addr-prefix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[ADDR.prefix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --addr-suffix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[ADDR.suffix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --background=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[BACKGROUND.${attribute}] ]]; then
                        #
                        # This option applies to the BYTE and TEXT fields, so two calls
                        # are needed here.
                        #
                        ByteSelector "BACKGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_BYTE_BACKGROUND"
                        ByteSelector "BACKGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_TEXT_BACKGROUND"
                    else
                        Error "background attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --byte=?*)
                if [[ $optarg =~ ^(binary|decimal|empty|hex|HEX|octal|xxd)(${regex_separator}(${regex_number}))?$ ]]; then
                    style="${BASH_REMATCH[1]}"
                    length="${BASH_REMATCH[3]}"
                    case "$style" in
                         binary) SCRIPT_STRINGS[BYTE.output]="BINARY";;
                        decimal) SCRIPT_STRINGS[BYTE.output]="DECIMAL";;
                          empty) SCRIPT_STRINGS[BYTE.output]="EMPTY";;
                            hex) SCRIPT_STRINGS[BYTE.output]="HEX-LOWER";;
                            HEX) SCRIPT_STRINGS[BYTE.output]="HEX-UPPER";;
                          octal) SCRIPT_STRINGS[BYTE.output]="OCTAL";;
                            xxd) SCRIPT_STRINGS[BYTE.output]="XXD";;
                              *) InternalError "option ${arg@Q} has not been completely implemented";;
                    esac
                    if [[ -n $length ]]; then
                        SCRIPT_STRINGS[DUMP.record.length]=$((length))
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --byte-background=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[BACKGROUND.${attribute}] ]]; then
                        ByteSelector "BACKGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_BYTE_BACKGROUND"
                    else
                        Error "background attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --byte-foreground=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[FOREGROUND.${attribute}] ]]; then
                        ByteSelector "FOREGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_BYTE_FOREGROUND"
                    else
                        Error "foreground attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --byte-prefix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[BYTE.prefix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --byte-separator=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[BYTE.separator]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --byte-suffix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[BYTE.suffix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            #
            # Options that start with --debug are undocumented and can be changed
            # or removed without notice. In addition, any description of debugging
            # options in comments may not be accurate.
            #

            --debug=?*)
                #
                # The expansion of optarg in the for loop replaces each comma with
                # a space.
                #
                for target in ${optarg//,/ }; do
                    case "$target" in
                          attributes) SCRIPT_STRINGS[DEBUG.attributes]="TRUE";;
                          background) SCRIPT_STRINGS[DEBUG.background]="TRUE";;
                             bytemap) SCRIPT_STRINGS[DEBUG.bytemap]="TRUE";;
                          foreground) SCRIPT_STRINGS[DEBUG.foreground]="TRUE";;
                              locals) SCRIPT_STRINGS[DEBUG.locals]="TRUE";;
                             handler) SCRIPT_STRINGS[DEBUG.handler]="TRUE";;
                             strings) SCRIPT_STRINGS[DEBUG.strings]="TRUE";;
                             textmap) SCRIPT_STRINGS[DEBUG.textmap]="TRUE";;
                        textmap-bash) SCRIPT_STRINGS[DEBUG.textmap-bash]="TRUE";;
                                time) SCRIPT_STRINGS[DEBUG.time]="TRUE";;
                          unexpanded) SCRIPT_STRINGS[DEBUG.unexpanded]="TRUE";;
                                 xxd) SCRIPT_STRINGS[DEBUG.xxd]="TRUE";;
                                   *) Error "target ${target@Q} in option ${arg@Q} is not recognized";;
                    esac
                done;;

            --debug-dump=?*)            # pick the dump handler
                case "$optarg" in
                    internal) SCRIPT_STRINGS[DEBUG.dump]="$optarg";;
                    selected) SCRIPT_STRINGS[DEBUG.dump]="$optarg";;
                         xxd) SCRIPT_STRINGS[DEBUG.dump]="$optarg";;
                           *) Error "argument ${optarg@Q} in option ${arg@Q} is not recognized";;
                esac;;

            --debug-token=?*)           # to trigger custom debugging code
                #
                # Expects code to be added somewhere in script that uses a regular
                # expression to test if the argument (bracketed by '[' and ']') is
                # currently in the string assigned to SCRIPT_STRINGS[DEBUG.token].
                # If so, your custom debugging code could be executed.
                #
                SCRIPT_STRINGS[DEBUG.token]+="[${optarg}]";;

            --foreground=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[FOREGROUND.${attribute}] ]]; then
                        #
                        # This option applies to the BYTE and TEXT fields, so two calls
                        # are needed here.
                        #
                        ByteSelector "FOREGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_BYTE_FOREGROUND"
                        ByteSelector "FOREGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_TEXT_FOREGROUND"
                    else
                        Error "foreground attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --help|-[?])
                Help;;

            --length=?*)
                if [[ $optarg =~ ^(${regex_number})$ ]]; then
                    SCRIPT_STRINGS[DUMP.record.length]=$((BASH_REMATCH[1]))
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --narrow)
                SCRIPT_STRINGS[DUMP.layout]="NARROW";;

            --newlines=?*)
                if [[ $optarg =~ ^(${regex_number})$ ]]; then
                    if (( optarg > 0 )); then
                        #
                        # shellcheck disable=SC2046
                        #
                        printf -v "SCRIPT_STRINGS[DUMP.record.separator]" '%.0s\n' $(command -p seq 1 "$optarg")
                    else
                        Error "argument ${optarg@Q} in option ${arg@Q} must be a positive integer"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --read=?*)
                if [[ $optarg =~ ^(${regex_number})$ ]]; then
                    SCRIPT_STRINGS[DUMP.input.count]=$((BASH_REMATCH[1]))
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --start=?*)
                if [[ $optarg =~ ^(${regex_number})(${regex_separator}(${regex_number}))?$ ]]; then
                    SCRIPT_STRINGS[DUMP.input.start]="$((BASH_REMATCH[1]))"
                    SCRIPT_STRINGS[DUMP.output.start]="$((${BASH_REMATCH[3]:-BASH_REMATCH[1]}))"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --text=?*)
                if [[ $optarg =~ ^(ascii|caret|empty|escape|unicode|xxd)(${regex_separator}(${regex_number}))?$ ]]; then
                    style="${BASH_REMATCH[1]}"
                    length="${BASH_REMATCH[3]}"
                    case "$style" in
                          ascii) SCRIPT_STRINGS[TEXT.output]="ASCII";;
                          caret) SCRIPT_STRINGS[TEXT.output]="CARET";;
                          empty) SCRIPT_STRINGS[TEXT.output]="EMPTY";;
                         escape) SCRIPT_STRINGS[TEXT.output]="CARET_ESCAPE";;
                        unicode) SCRIPT_STRINGS[TEXT.output]="UNICODE";;
                            xxd) SCRIPT_STRINGS[TEXT.output]="XXD";;
                              *) InternalError "option ${arg@Q} has not been completely implemented";;
                    esac
                    if [[ -n $length ]]; then
                        SCRIPT_STRINGS[DUMP.record.length]=$((length))
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --text-background=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[BACKGROUND.${attribute}] ]]; then
                        #
                        # The "TEXT_" prefix that's added here restricts changes
                        # to the TEXT field.
                        #
                        ByteSelector "BACKGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_TEXT_BACKGROUND"
                    else
                        Error "background attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --text-foreground=?*)
                if [[ $optarg =~ ^([[:alpha:]]+([-][[:alpha:]]+)*)(${regex_separator}(.*))?$ ]]; then
                    attribute="${BASH_REMATCH[1]}"
                    selector="${BASH_REMATCH[4]}"
                    if [[ -v SCRIPT_ANSI_ESCAPE[FOREGROUND.${attribute}] ]]; then
                        #
                        # The "TEXT_" prefix restricts changes to the TEXT field.
                        #
                        ByteSelector "FOREGROUND.${attribute}" "$selector" "SCRIPT_ATTRIBUTES_TEXT_FOREGROUND"
                    else
                        Error "foreground attribute ${attribute@Q} in option ${arg@Q} is not recognized"
                    fi
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} is not recognized"
                fi;;

            --text-prefix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[TEXT.prefix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --text-suffix=*)
                if [[ $optarg =~ ^([[:print:]])*$ ]]; then
                    SCRIPT_STRINGS[TEXT.suffix]="$optarg"
                else
                    Error "argument ${optarg@Q} in option ${arg@Q} contains unprintable characters"
                fi;;

            --wide)
                SCRIPT_STRINGS[DUMP.layout]="${SCRIPT_STRINGS[DUMP.layout-xxd]}";;

            #
            # Remaining patterns are pretty standard.
            #

             -) break;;                 # stdin abbreviation
            --) shift; break;;
            -*) Error "invalid option ${arg@Q}";;
             *) break;;
        esac
        shift
    done

    SCRIPT_ARGUMENTS_CONSUMED=$((argc - $#))
}

Setup() {
    local name

    #
    # This is the first function called by Main. It's where initialization that
    # should follow the script's global variable declarations but happen before
    # command line options are processed, can be handled.
    #

    SCRIPT_STRINGS[INFO.initial.keys]="${!SCRIPT_STRINGS[*]} "  # trailing space is required

    if [[ -n ${SCRIPT_STRINGS[DUMP.required.commands]} ]]; then
        #
        # Only supposed to check for commands that are needed to produce the
        # actual dump - commands used for debugging or by the imported code
        # don't count.
        #
        for name in ${SCRIPT_STRINGS[DUMP.required.commands]}; do
            if ! command -vp "$name" >/dev/null; then
                Error "required command named ${name@Q} is not available"
            fi
        done
    fi
}

##############################
#
# Exportable Code
#
##############################

#
# New function that eventually might end up in my private bash library. It was
# written quickly, so don't take it too seriously, but it seems like there are
# some useful features waiting to be implemented. Kind of doubt I'll find the
# time (or energy) to work on it, but there's always a chance.
#

HelpScanner() {
    local arg
    local connected
    local help_arg
    local -a help_content
    local -A help_footnote
    local help_text
    local help_trigger
    local line
    local value

    #
    # Reads lines from standard input looking for the ones that start with the
    # string stored in help_trigger. All lines are matched when help_trigger is
    # the empty string. Matching lines, with the trigger removed, are stored in
    # an array that's eventually written to standard output.
    #
    # The caller gets to pick where the lines come from (e.g., a here document,
    # a simple bash string, a bash script file) by redirecting standard input to
    # wherever the text is stored when it calls this function.
    #

    connected="TRUE"
    help_content=()
    help_footnote=()
    help_trigger=""

    while (( $# > 0 )); do
        arg=""
        if [[ $1 =~ ^[-+][^=]+[=](.+)$ ]]; then
            arg="${BASH_REMATCH[1]}"
        fi
        case "$1" in
            +connected)
                connected="TRUE";;

            -connected)
                connected="FALSE";;

            +copyright)
                help_footnote[Copyright]="Copyright: None";;

            -copyright)
                unset "help_footnote[Copyright]";;

            +license)
                help_footnote[License]="License: None";;

            -license)
                unset "help_footnote[License]";;

            +trigger)
                help_trigger="#@#";;

            -trigger=*)
                help_trigger="$arg";;

            --) shift; break;;
            +*) ;;                      # silently ignored
            -*) ;;                      # silently ignored
             *) break;;
        esac
        shift
    done

    while IFS="" read -r line; do
        if [[ $line =~ ^"${help_trigger}"(.*)$ ]]; then
            if [[ -n $help_trigger ]]; then
                help_arg="${BASH_REMATCH[1]:0:1}"           # currently unused
                help_text="${BASH_REMATCH[1]:1}"
            else
                help_arg=""                                 # currently unused
                help_text="${BASH_REMATCH[1]}"
            fi
            if (( ${#help_content[@]} > 0 )) || [[ -n $help_text ]]; then
                help_content+=("$help_text")
            fi
        elif [[ $connected == "FALSE" ]] || (( ${#help_content[@]} == 0 )); then
            #
            # Can't get here if help_trigger is empty.
            #
            if [[ $line =~ ^[#]+[[:blank:]]+("Copyright "(.+))$ ]]; then
                value="${BASH_REMATCH[1]}"
                if [[ -n ${help_footnote[Copyright]} ]]; then
                    help_footnote[Copyright]="${value}"
                fi
            elif [[ $line =~ ^[#]+[[:blank:]]+("License: "(.+))$ ]]; then
                value="${BASH_REMATCH[1]}"
                if [[ -n ${help_footnote[License]} ]]; then
                    help_footnote[License]="${value}"
                fi
            fi
        else
            break
        fi
    done

    #
    # shellcheck disable=SC2059
    #
    if (( ${#help_content[@]} > 0 )); then
        #
        # Avoiding printf's %b format should prevent accidental expansions in any
        # of the output we generate.
        #
        printf "${help_content[0]:+\n}"
        printf "%s\n" "${help_content[@]}"
        if (( ${#help_footnote[@]} > 0 )); then
            printf "${help_content[-1]:+\n}"
            printf "${help_footnote[Copyright]:+%s\n}" "${help_footnote[Copyright]:+${help_footnote[Copyright]}}"
            printf "${help_footnote[License]:+%s\n}" "${help_footnote[License]:+${help_footnote[License]}}"
            printf "\n"
        fi
    fi
}

##############################
#
# Imported Library Code
#
##############################

#
# The functions, global variables, and the initialization code in this section
# came from a private bash library that I sometimes use, but the full library
# isn't anywhere close to ready to share. However, the library's error handling
# code is useful, so I grabbed some it, made a few simple changes, and included
# it in this section. There's much more here than the script needs, particularly
# in the Message function, but I didn't try to remove any unused code because I
# wanted to make it easy to eventually port improvements back into my library.
#
# My goal with these library functions was modest. Be able to write a one line
# message TO THE USER on standard error (or even standard output) that includes
# information about what happened and where it happened, and then terminate the
# program. None of it even has to happen automatically, but I want one function
# call to handle it all. Despite how simple that sounds, doing it reliably in a
# bash script is harder than you might expect. Pipes, subshells, and redirection
# are just a few reasons why things can get tricky. Anyway, I'm not certain this
# satisfies all my goals, but at the very least it's a reasonable attempt.
#
# As far as I can tell, there really is no formal way to deal with errors that
# crop up in a bash script. There are plenty of suggestions, like use "set -e",
# "set -u", and "set -o pipefail", but if you look around a bit on the web you
# can easily find compelling warnings about many suggested solutions. There's a
# good technical discussion in
#
#     https://mywiki.wooledge.org/BashFAQ/105
#
# The first three or four paragraphs and the last two (short) sections are easy
# reading - they're worth a look if you're curious about some of the issues.
#

declare -A MESSAGE_STRINGS=(
    [STDOUT.file]="/dev/stdout"
    [STDERR.file]="/dev/stderr"
    [STDOUT.descriptor]="1"
    [STDERR.descriptor]="2"

    #
    # Default state settings.
    #

    [STATE.output]="-stderr"
    [STATE.prefix]="$(command -p basename "${BASH_SOURCE[0]}")"
)

#
# Next few lines are a bit tricky. Ideally what happens is file descriptors 1 and
# 2 are duplicated and automatically stored in the MESSAGE_STRINGS array (that's
# what each exec call is doing). No matter what happens during the file descriptor
# duplication, all of the output targeted at standard error or standard output by
# the messaging functions is sent to the file descriptors that are stored in the
# MESSAGE_STRINGS array.
#
# NOTE - remembering file descriptors that are duplicated before much else happens
# means messaging should behave the way you would expect, even if these functions
# are called from a subshell. Anyway, that's the idea.
#

if [[ $BASH_SUBSHELL == "0" ]]; then
    #
    # NOTE - shellcheck (version 0.8.0) and the bash man page don't agree about
    # the {varname} syntax used to save file descriptors in the exec calls. The
    # bash man page describes it in the REDIRECTION section, and even though an
    # official definition of "varname" seems to be missing (anywhere in the man
    # page) I doubt shellcheck is correct.
    #
    if [[ ${MESSAGE_STRINGS[STDOUT.descriptor]} == "1" ]]; then
        #
        # shellcheck disable=SC1083 disable=SC2102
        #
        exec {MESSAGE_STRINGS[STDOUT.descriptor]}>&1;
    fi
    if [[ ${MESSAGE_STRINGS[STDERR.descriptor]} == "2" ]]; then
        #
        # shellcheck disable=SC1083 disable=SC2102
        #
        exec {MESSAGE_STRINGS[STDERR.descriptor]}>&2
    fi

    #
    # A recent experiment that replaces bash's default SIGTERM signal handler
    # with a quieter, but otherwise equivalent, implementation. Definitely not
    # important, because all it really does is eliminate the word "Terminated"
    # that would annoyingly appear after Error notices it's in a subshell and
    # uses kill to try to stop the program. Nothing changes if it looks like a
    # SIGTERM signal handler has been already been set.
    #
    if [[ -z $(trap -p SIGTERM 2>/dev/null) ]]; then
        #
        # shellcheck disable=SC2064
        #
        trap "exit $((128 + 15))" SIGTERM
    fi
fi

Error() {
    local exit
    local -a options

    #
    # Being able to determine when we're running in a subshell or not is important
    # if the program is supposed to exit after the error is explained to the user.
    # At least two bash variables (i.e., BASHPID and BASH_SUBSHELL) should be able
    # answer the question. However, I don't think either one is guaranteed to work
    # because both lose their special properties if they're unset. It's behavior I
    # can't explain, but I don't think it's anything we need to worry about here.
    #

    exit="TRUE"
    options=()

    while (( $# > 0 )); do
        case "$1" in
            +exit) exit="TRUE";;
            -exit) exit="FALSE";;
               --) shift; break;;
               +*) options+=("$1");;
               -*) options+=("$1");;
                *) break;;
        esac
        shift
    done

    Message -tag="Error" -info=line -escapes -stderr "${options[@]}" +frame -- "$@"

    if [[ $exit == "TRUE" ]]; then
        #
        # Checking BASHPID, instead of BASH_SUBSHELL, feels more appropriate here.
        #
        if [[ $BASHPID != "$$" ]]; then
            #
            # Looks like we're in a subshell, which means just exiting won't stop
            # the program. Sending SIGTERM to the program probably is all we should
            # do here.
            #
            kill -TERM $$
        fi
        exit 1
    fi
}

InternalError() {
    local -a options

    options=()

    while (( $# > 0 )); do
        case "$1" in
            --) shift; break;;
            +*) options+=("$1");;
            -*) options+=("$1");;
             *) break;;
        esac
        shift
    done

    Error -tag="InternalError" -info=location +exit "${options[@]}" +frame -- "$@"
}

Message() {
    local arg
    local -A caller
    local date
    local format
    local frame
    local info
    local message
    local optarg
    local output
    local pid
    local prefix
    local quiet
    local suffix
    local tag
    local time
    local token

    #
    # This is the preferred way to write short messages, that usually include the
    # program name as a prefix, to file descriptors that were originally assigned
    # to standard output or standard error. It should work anywhere, including in
    # subshells, so it's also used by the functions responsible for error and log
    # messages.
    #
    # The various frame options can be used to adjust the index that this function
    # uses to extract information from the call stack or the FUNCNAME array that's
    # supposed to be included in the final message. When functions, like the ones
    # for error (or log messages), call this function they usually add the +frame
    # option to the arguments to try to make sure any call stack information added
    # to the final message is actually useful.
    #
    # NOTE - this function was designed to be the final step for things like error
    # or log file messages, so many options (e.g., +frame, -date, -pid) don't make
    # sense in all situations.
    #
    # NOTE - we intentionally don't support arbitrary date formats. Didn't want to
    # try to validate them, particularly when the caller can do the formatting and
    # then use the -date= option to hand us a properly formatted date string.
    #

    caller=()
    date=""
    format="%b\n"
    frame="0"
    info=""
    output="${MESSAGE_STRINGS[STATE.output]}"
    pid=""
    prefix="${MESSAGE_STRINGS[STATE.prefix]}"
    quiet="FALSE"
    suffix=""
    tag=""
    time=""

    while (( $# > 0 )); do
        arg="$1"
        if [[ $arg =~ ^[-+]([^=]+)[=](.+)$ ]]; then
            optarg="${BASH_REMATCH[2]}"
        else
            optarg=""
        fi
        case "$arg" in
            +date)
                date="[$(LC_ALL=C command -p date)]";;

            +date-utc)
                date="[$(LC_ALL=C command -p date -u)]";;

            -date)
                date="";;

            -date=?*)
                date="[${optarg}]";;

            +escapes)
                format="%b\n";;

            -escapes)
                format="%s\n";;

            +frame)
                frame=$((frame + 1));;

            -frame)
                frame="0";;

            -info)
                info="";;

            -info=?*)
                info="${optarg}";;

            +pid)
                pid="[$$]";;

            -pid)
                pid="";;

            -pid=?*)
                pid="[${optarg}]";;

            -pid-width=[01234567])      # Linux max fits in 7 decimal digits
                printf -v pid "[%*s]" "${optarg}" "$$";;

            -prefix)
                prefix="";;

            -prefix=?*)
                prefix="${optarg}";;

            +quiet)
                quiet="TRUE";;

            -quiet)
                quiet="FALSE";;

            -stderr)
                output="$arg";;

            -stdout)
                output="$arg";;

            -suffix)
                suffix="";;

            -suffix=?*)
                suffix="${optarg}";;

            -tag)
                tag="";;

            -tag=?*)
                tag="${optarg}";;

            +time)
                time="[$(LC_ALL=C command -p date '+%s')]";;

            +time-ns)
                time="[$(LC_ALL=C command -p date '+%s.%N')]";;

            -time)
                time="";;

            -time=?*)
                time="[${optarg}]";;

            --) shift; break;;
            +*) ;;
            -*) ;;
             *) break;;
        esac
        shift
    done

    message="$*"

    #
    # Checking $quiet here means the message can be hidden, no matter where it's
    # supposed to go, using the +quiet option.
    #

    if [[ -n $message ]] && [[ $quiet != "TRUE" ]]; then
        if [[ -n "$info" ]]; then
            #
            # Use the value stored in frame to index into the bash "call stack".
            # The rules that determine the actual numbers used to index into the
            # three bash arrays aren't completely obvious, but they seem to work
            # and agree with the output of bash's caller builtin. The benefit of
            # doing things this way is we don't have to parse the output of the
            # caller builtin (using a regular expression) to grab each "field".
            #

            caller[LINE]="${BASH_LINENO[$frame]}"
            caller[FUNCTION]="${FUNCNAME[$((frame + 1))]}"
            caller[SOURCE]="${BASH_SOURCE[$((frame + 1))]}"

            for token in ${info//,/ }; do
                case "${token^^}" in
                    CALLER)
                        if [[ -n ${caller[LINE]} ]] && [[ -n ${caller[FUNCTION]} ]] && [[ -n ${caller[SOURCE]} ]]; then
                            tag="${tag:+${tag}] [}${caller[SOURCE]}; ${caller[FUNCTION]}; Line ${caller[LINE]}"
                        fi;;

                    FUNCTION)
                        if [[ -n ${caller[FUNCTION]} ]]; then
                            tag="${tag:+${tag}] [}${caller[FUNCTION]}"
                        fi;;

                    LINE)
                        if [[ -n ${caller[LINE]} ]]; then
                            tag="${tag:+${tag}] [}Line ${caller[LINE]}"
                        fi;;

                    LOCATION)
                        if [[ -n ${caller[LINE]} ]] && [[ -n ${caller[SOURCE]} ]]; then
                            tag="${tag:+${tag}] [}${caller[SOURCE]}; Line ${caller[LINE]}"
                        fi;;

                    SOURCE)
                        if [[ -n ${caller[SOURCE]} ]]; then
                            tag="${tag:+${tag}] [}${caller[SOURCE]}"
                        fi;;
                esac
            done
        fi

        message="${date:+${date} }${time:+${time} }${pid:+${pid} }${prefix:+${prefix}: }${message}${tag:+ [${tag}]}${suffix}"

        #
        # shellcheck disable=2059
        #
        case "${output}" in
            -stderr)
                printf -- "${format}" "${message}" 1>&"${MESSAGE_STRINGS[STDERR.descriptor]:-2}";;

            -stdout)
                printf -- "${format}" "${message}" 1>&"${MESSAGE_STRINGS[STDOUT.descriptor]:-1}";;

             *) printf -- "${format}" "${message}" 1>&"${MESSAGE_STRINGS[STDERR.descriptor]:-2}";;
        esac
    fi
}

##############################
#
# Script Start
#
##############################

Main "$@"
exit 0                  # skip everything else in this file

##############################
#
# Script Documentation
#
##############################

#@#
#@# SYNOPSIS
#@# ========
#@#
#@# bytedump [OPTIONS] [FILE|-]
#@#
#@# DESCRIPTION
#@# ===========
#@#
#@# A program that generates a dump of the bytes in FILE, which must be a readable file
#@# that's not a directory. Standard input is read if the argument is missing or equal
#@# to '-'.
#@#
#@# This version of the program is a bash script. It uses the xxd command to generate a
#@# dump that's usually postprocessed to produce the final output.
#@#
#@# OPTIONS
#@# =======
#@#
#@# Options are processed in the order they appear on the command line and options that
#@# are processed later take precedence. Option processing continues until there are no
#@# more arguments that start with a '-' character. The option '--', which is otherwise
#@# ignored, can be used to mark the end of the options. The argument '-', which always
#@# stands for standard input, also ends option processing.
#@#
#@# The documented options are:
#@#
#@#     --addr=<style>
#@#     --addr=<style>:<width>
#@#         The <style> controls how addresses are displayed. It must be one of the
#@#         case-dependent strings in the list:
#@#
#@#             decimal - addresses are displayed in decimal (width=6)
#@#                 hex - addresses are displayed in lowercase hex (width=6)
#@#                 HEX - addresses are displayed in uppercase hex (width=6)
#@#               octal - addresses are displayed in octal (width=6)
#@#                 xxd - addresses are displayed in lowercase hex (width=08)
#@#
#@#               empty - address fields are all empty
#@#
#@#         The <width> that can be set using this option is the minimum <width> of
#@#         the field, in digits, that's allocated for addresses. Each one is right
#@#         justified in that field and padded on the left with spaces, or zeros, to
#@#         fill the field. Zeros are only used for padding when the first digit of
#@#         the <width> is 0, as it is in the xxd <style>.
#@#
#@#         The default <style> is hex and the default <width>, for each <style>, is
#@#         shown in the list.
#@#
#@#     --addr-prefix=<string>
#@#         Prepend <string> to the address in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can
#@#         be empty. The default prefix is the empty string.
#@#
#@#     --addr-suffix=<string>
#@#         Append <string> to the address in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can
#@#         be empty. The default suffix is a single colon (i.e., ":").
#@#
#@#     --background=<color>:<selector>
#@#         Sets the background <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's byte or text fields.
#@#
#@#         The background <color> should be one of the names in the table
#@#
#@#             red            alt-red            bright-red
#@#             green          alt-green          bright-green
#@#             blue           alt-blue           bright-blue
#@#             cyan           alt-cyan           bright-cyan
#@#             magenta        alt-magenta        bright-magenta
#@#             yellow         alt-yellow         bright-yellow
#@#             black          alt-black          bright-black
#@#             white          alt-white          bright-white
#@#
#@#         or the word
#@#
#@#             reset
#@#
#@#         which cancels any background color already assigned to the selected bytes.
#@#         The relative brightness of a <color> tends to increase as you move to the
#@#         right in the table.
#@#
#@#         The <selector> is documented below in the SELECTORS section.
#@#
#@#     --byte=<style>
#@#     --byte=<style>:<length>
#@#         The <style> controls how each byte is displayed and it must be one of the
#@#         case-dependent strings in the list:
#@#
#@#              binary - bytes are displayed in binary (base 2)
#@#             decimal - bytes are displayed in decimal (base 10)
#@#                 hex - bytes are displayed in lowercase hex (base 16)
#@#                 HEX - bytes are displayed in uppercase hex (base 16)
#@#               octal - bytes are displayed in octal (base 8)
#@#                 xxd - bytes are displayed in lowercase hex (base 16)
#@#
#@#               empty - byte fields are all empty. The byte and text fields can't
#@#                       both be empty.
#@#
#@#         The optional <length> must be a nonnegative integer that's used to set the
#@#         maximum length of each record in the dump. See the --length option for more
#@#         details.
#@#
#@#     --byte-background=<color>:<selector>
#@#         Sets the background <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's byte field.
#@#
#@#         The available <color> choices are listed under the --background option's
#@#         description. The <selector> is documented below in the SELECTORS section.
#@#
#@#     --byte-foreground=<color>:<selector>
#@#         Sets the foreground <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's byte field.
#@#
#@#         The available <color> choices are listed under the --foreground option's
#@#         description. The <selector> is documented below in the SELECTORS section.
#@#
#@#     --byte-prefix=<string>
#@#         Prepend <string> to the byte field in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can be
#@#         empty. The default prefix is the empty string.
#@#
#@#     --byte-separator=<string>
#@#         Use <string> to separate individual bytes in every record that's included
#@#         in the dump. All characters in <string> must be printable or the <string>
#@#         can be empty. The default separator is a single space (i.e., " ").
#@#
#@#     --byte-suffix=<string>
#@#         Append <string> to the byte field in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can be
#@#         empty. The default suffix is the empty string.
#@#
#@#     --foreground=<color>:<selector>
#@#         Sets the foreground <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's byte or text fields.
#@#
#@#         The foreground <color> should be one of the names in the table
#@#
#@#             red            alt-red            bright-red
#@#             green          alt-green          bright-green
#@#             blue           alt-blue           bright-blue
#@#             cyan           alt-cyan           bright-cyan
#@#             magenta        alt-magenta        bright-magenta
#@#             yellow         alt-yellow         bright-yellow
#@#             black          alt-black          bright-black
#@#             white          alt-white          bright-white
#@#
#@#             blink-red      blink-alt-red      blink-bright-red
#@#             blink-green    blink-alt-green    blink-bright-green
#@#             blink-blue     blink-alt-blue     blink-bright-blue
#@#             blink-cyan     blink-alt-cyan     blink-bright-cyan
#@#             blink-magenta  blink-alt-magenta  blink-bright-magenta
#@#             blink-yellow   blink-alt-yellow   blink-bright-yellow
#@#             blink-black    blink-alt-black    blink-bright-black
#@#             blink-white    blink-alt-white    blink-bright-white
#@#
#@#         or the word
#@#
#@#             reset
#@#
#@#         which cancels any foreground color already assigned to the selected bytes.
#@#         The relative brightness of a <color> tends to increase as you move to the
#@#         right in the table. Foreground colors that start with the "blink-" prefix
#@#         cause all characters displayed in that <color> to blink.
#@#
#@#         The <selector> is documented below in the SELECTORS section.
#@#
#@#      -?
#@#     --help
#@#         Print the internal documentation about the script on standard output and
#@#         then exit.
#@#
#@#     --length=<length>
#@#         A <length> that's a positive integer is the maximum number of bytes, read
#@#         from the input file, that can be displayed in a single record. Each record
#@#         in a dump, except perhaps the last one, represents exactly <length> input
#@#         file bytes. Each record's byte and text field components are two different
#@#         ways that the dump can use to display those <length> input file bytes.
#@#
#@#         The default <length> is 16. The maximum <length>, which is imposed by xxd,
#@#         is 256. When <length> is 0, the entire input file is displayed in a single
#@#         record.
#@#
#@#     --narrow
#@#         Each record in the dump starts on a new line, but in this layout style
#@#         only the address and byte fields are printed next to each other on that
#@#         line. The text field prints on the next line, but everything is carefully
#@#         adjusted so each character ends up directly below its corresponding byte.
#@#         The vertical alignment constraints mean that strings set using unrelated
#@#         options (e.g., --byte-separator) may occasionally have to be padded with
#@#         spaces.
#@#
#@#         This option is ignored and the default layout style, which is described
#@#         in the --wide option, is used whenever the byte field or the text field
#@#         is empty.
#@#
#@#     --newlines=<count>
#@#         Use <count> newlines to separate records in the dump. <count> must be a
#@#         positive integer. The default <count> is 1.
#@#
#@#     --read=<count>
#@#         Stop the dump after reading <count> bytes from the input file. <count>
#@#         must be a nonnegative integer and when it's 0 the entire input file is
#@#         read. The default <count> is 0.
#@#
#@#     --start=<address>
#@#     --start=<address>:<output-address>
#@#         Start the dump with the byte at <address> in the input file and use it,
#@#         or the optional <output-address>, as the address that's attached to the
#@#         first record in the output dump. The <address> and <output-address> must
#@#         be nonnegative integers. The default <address> is 0.
#@#
#@#     --text=<style>
#@#     --text=<style>:<length>
#@#         The <style> selects the character strings that are used to represent each
#@#         byte in the dump's text field. It must be one of the names in the list:
#@#
#@#               ascii - one character strings that only identify bytes that are
#@#                       printable ASCII characters. All other bytes, even the ones
#@#                       that are classified as printable Unicode code points, are
#@#                       represented by a period (i.e., ".").
#@#
#@#             unicode - one character strings that identify all bytes that represent
#@#                       printable Unicode code points using the character assigned to
#@#                       to that code point. The rest of the bytes are all represented
#@#                       using a period (i.e., ".").
#@#
#@#                       Any byte representing a character that bash determines can't
#@#                       be displayed in the user's locale is replaced by one question
#@#                       mark (i.e., "?").
#@#
#@#               caret - two character strings that use a custom extension of caret
#@#                       notation to uniquely represent every byte. Bytes that are
#@#                       printable Unicode code points are represented by character
#@#                       strings that start with a space and end with the printable
#@#                       Unicode character assigned to that byte.
#@#
#@#                       The rest of the bytes are assigned to Unicode code points
#@#                       that aren't printable characters. The two character strings
#@#                       used to represent them all start with a caret (i.e., "^")
#@#                       and end with a unique printable character that's selected
#@#                       using the formula
#@#
#@#                           (byte + 0x40) % 0x80
#@#
#@#                       when the unprintable byte is ASCII (i.e., byte < 0x80) and
#@#
#@#                           (byte + 0x40) % 0x80 + 0x80
#@#
#@#                       when it's not ASCII (i.e., byte >= 0x80). The extension of
#@#                       caret notation beyond ASCII was done to make sure each byte
#@#                       in an input file could be identified from the two character
#@#                       strings displayed in the text field. It's not a necessary
#@#                       constraint, because the byte field is always available, but
#@#                       it seems like reasonable goal.
#@#
#@#                       Any byte representing a character that bash determines can't
#@#                       be displayed in the user's locale is replaced by two question
#@#                       marks (i.e., "??").
#@#
#@#              escape - two character strings that use C-style escapes to represent
#@#                       unprintable bytes with numeric values that are the same as
#@#                       two character C-style backslash escape sequences. All other
#@#                       bytes are represented by two character strings described in
#@#                       the caret <style> section.
#@#
#@#                       Any byte representing a character that bash determines can't
#@#                       be displayed in the user's locale is replaced by two question
#@#                       marks (i.e., "??").
#@#
#@#                 xxd - duplicates the text field display style that the xxd command
#@#                       produces. It's just a synonym for ascii and is only included
#@#                       for consistency with the --addr and --byte options.
#@#
#@#               empty - text fields are all empty. The text and byte fields can't
#@#                       both be empty.
#@#
#@#         The default <style> is ascii.
#@#
#@#         The optional <length> must be a nonnegative integer that's used to set the
#@#         maximum length of each record in the dump. See the --length option for more
#@#         details.
#@#
#@#     --text-background=<color>:<selector>
#@#         Sets the background <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's text field.
#@#
#@#         The available <color> choices are listed under the --background option's
#@#         description. The <selector> is documented below in the SELECTORS section.
#@#
#@#     --text-foreground=<color>:<selector>
#@#         Sets the foreground <color> that's used when any of the bytes selected by
#@#         <selector> are displayed in the dump's text field.
#@#
#@#         The available <color> choices are listed under the --foreground option's
#@#         description. The <selector> is documented below in the SELECTORS section.
#@#
#@#     --text-prefix=<string>
#@#         Prepend <string> to the text field in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can be
#@#         empty. The default prefix is the empty string.
#@#
#@#     --text-suffix=<string>
#@#         Append <string> to the text field in every record that's included in the
#@#         dump. All characters in <string> must be printable or the <string> can be
#@#         empty. The default suffix is the empty string.
#@#
#@#     --wide
#@#         Each record in a dump always starts on a new line and in this layout the
#@#         address, byte, and text field components are arranged in that order and
#@#         printed on the same line. This is the default layout.
#@#
#@#         The implementation of this layout is straightforward and doesn't require
#@#         changes to settings specified by unrelated options. The --narrow option
#@#         can be used to request the other supported layout.
#@#
#@# SELECTORS
#@# =========
#@#
#@# All background and foreground options expect to find a <selector> in their argument
#@# that picks all the bytes targeted by that option. The <selector> consists of one or
#@# more space separated tokens that can be integers, integer ranges, character classes,
#@# or raw strings (that use a slightly modifed notation borrowed from Rust).
#@#
#@# A <selector> that starts with an optional base prefix and is followed by tokens that
#@# are completely enclosed in a single set of parentheses picks the base that's used to
#@# evaluate every integer and integer range token in <selector>. For example,
#@#
#@#     0x(token1 token2 token3 token4 ...)
#@#
#@# is valid syntax that uses "0x" to pick the base used to evaluate every number in the
#@# tokens that are enclosed in parentheses. A base prefix that's "0x" or "0X" means all
#@# numbers are hex, "0" means they're all octal, and no base prefix means the numbers
#@# are all decimal. If a base is set using this syntax, every character that appears in
#@# every number in the tokens must be a valid digit in that base.
#@#
#@# Any of the following tokens are recognized in a <selector>:
#@#
#@#     Integer
#@#         Whenever a <selector> has set a base, using the syntax described above, all
#@#         characters in every integer token in that <selector> must be digits in that
#@#         base. Otherwise C-style syntax is used, so hex integers start with "0x" or
#@#         "0X", octal integers start with "0", and decimal integers always start with
#@#         a nonzero decimal digit. Integers that don't represent a byte are ignored.
#@#
#@#     Integer Range
#@#         A pair of integer tokens separated by '-' represents a closed interval that
#@#         extends from the left end point to the right end point. Both end points of
#@#         an integer range must be written in the same base. All rules that apply to
#@#         integer tokens apply to both end points. Any part of an integer range that
#@#         doesn't represent a byte is ignored.
#@#
#@#     Character Class
#@#         A character class uses a short, familiar lowercase name to select a group
#@#         of bytes. Those names must be bracketed by "[:" and ":]" in the <selector>
#@#         to be recognized as a character class. The 15 character classes that are
#@#         allowed in a <selector> are:
#@#
#@#             [:alnum:]      [:digit:]      [:punct:]
#@#             [:alpha:]      [:graph:]      [:space:]
#@#             [:blank:]      [:lower:]      [:upper:]
#@#             [:cntrl:]      [:print:]      [:xdigit:]
#@#
#@#             [:ascii:]      [:latin1:]     [:all:]
#@#
#@#         The first four rows are the 12 character classes that are defined in the
#@#         POSIX standard. They should be familiar because they're supported by most
#@#         regular expression implementations. The last row are 3 character classes
#@#         that we support because they seem like a convenient way to select familiar
#@#         (or otherwise obvious) blocks of contiguous bytes.
#@#
#@#     Raw String
#@#         A modified version of Rust's raw string literal can be used as a token in a
#@#         <selector>. They start with a prefix that's the letter 'r', zero or more '#'
#@#         characters, and a single or double quote, and always end with a suffix that
#@#         matches the quote and the number of '#' characters used in the prefix. For
#@#         example,
#@#
#@#               r"aeiouAEIOU"
#@#               r'aeiouAEIOU'
#@#              r#'aeiouAEIOU'#
#@#             r##"aeiouAEIOU"##
#@#
#@#         are raw string tokens that select all the bytes that represent vowels. Any
#@#         character, except null, can appear in a raw string. The selected bytes are
#@#         the Unicode code points of the characters in the string that are less than
#@#         256.
#@#
#@# A <selector> can contain one or more of these tokens, so that means there are lots
#@# of equivalent ways to select bytes. For example, the command line options
#@#
#@#     --text-foreground="bright-red:    r'aeiou@0123456789'"
#@#     --text-foreground="bright-red:    r'aeiou'         r'@' r'0123456789'"
#@#     --text-foreground="bright-red:    r'aeiou'         r'@' [:digit:]"
#@#     --text-foreground="bright-red:    r'aeiou'         0x40  0x30-0x39"
#@#     --text-foreground="bright-red: 0x(r'aeiou'           40    30-39)"
#@#     --text-foreground="bright-red: 0x(61 65 69 6F 75     40    30-39)"
#@#
#@# all select exactly the same bytes, namely the lowercase vowels, the '@' character,
#@# and the decimal digits, and arrange for "bright-red" to be their foreground color
#@# whenever any of them are displayed in the text field.
#@#
#@# DEBUGGING
#@# =========
#@#
#@# Even though the program's debugging support is officially undocumented, there are
#@# a few debug options that users might occasionally be interested in. The three that
#@# stand out can be added individually
#@#
#@#     bytedump --debug=xxd --debug=bytemap --debug=textmap ...
#@#
#@# or in a comma separated list
#@#
#@#     bytedump --debug=xxd,bytemap,textmap ...
#@#
#@# to any of the example command lines in the next section. Debugging output goes to
#@# standard error, so it can easily be separated from the generated dump. Organization
#@# of the debugging output is controlled internally by the program and does not depend
#@# the command line ordering of the debug options.
#@#
#@# EXAMPLES
#@# ========
#@#
#@# If you run the program without any command line options, as in
#@#
#@#     bytedump file
#@#
#@# you get a dump of file using the program's default settings. It's not a dump that
#@# xxd can duplicate, because the addresses won't exactly match and there aren't xxd
#@# options that change how it displays addresses (always 8 digit, 0 padded, lowercase
#@# hex numbers). However, options can control the way this script displays addresses,
#@# so if you run
#@#
#@#     bytedump --addr=xxd file
#@#
#@# or equivalently
#@#
#@#     bytedump --addr=hex:08 file
#@#
#@# you get a dump that exactly matches the output that
#@#
#@#     xxd -c16 -g1 file
#@#
#@# generates. Add --debug=xxd to the command line and the program will show you exactly
#@# how xxd was called and let you know if xxd's output was postprocessed to produce the
#@# produce the final dump.
#@#
#@# There are options that give you quite a bit of control over the address, byte, and
#@# text fields in a dump. For example,
#@#
#@#     bytedump --addr=decimal --byte=HEX --text=caret file
#@#
#@# prints decimal addresses, uppercase hex bytes, and uses caret notation to represent
#@# bytes that are displayed in the text field. All three fields will be displayed next
#@# to each other on a line, but add the --narrow option to the command line
#@#
#@#     bytedump --narrow --addr=decimal --byte=HEX --text=caret file
#@#
#@# and the layout changes. The text field prints on a line by itself and everything is
#@# adjusted, by stretching and translation, so every byte displayed in the byte field
#@# and its representation in the text field end up in the same column.
#@#
#@# Any of the fields can be hidden by setting them to empty, so
#@#
#@#     bytedump --addr=decimal --byte=binary --text=empty file
#@#
#@# prints decimal addresses, binary (base 2) bytes, and hides the text field, while
#@#
#@#     bytedump --addr=empty --byte=binary --text=empty file
#@#
#@# just prints the binary (base 2) representation of the bytes. Hiding both the byte
#@# and text fields doesn't make sense and will result in an error.
#@#
#@# SEE ALSO
#@# ========
#@#
#@# ascii(7), hexdump(1), iso_8859-1(7), od(1), xxd(1)
#@#

