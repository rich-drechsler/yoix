#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

SOURCE = \
	ywait_clipboard_help._TXT \
	ywait_comment_help._TXT \
	ywait_main_help._TXT \
	ywait_message_help._TXT \
	ywait_preferences_help._TXT \
	ywait_printing_help._TXT

ALLFILES = $(SOURCE:._TXT=.txt)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/help

