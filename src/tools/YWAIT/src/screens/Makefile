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
	ywait_calendar.yx \
	ywait_clipboard.yx \
	ywait_debugger.yx \
	ywait_edit_preferences.yx \
	ywait_pane2screen.yx \
	ywait_screen_template1.yx \
	ywait_screen_template2.yx \
	ywait_screen_template3.yx \
	ywait_screen_template3a.yx \
	ywait_screen_template4.yx \
	ywait_screen_template4a.yx \
	ywait_screen_template5.yx \
	ywait_screen_template6.yx \
	ywait_selector_list.yx \
	ywait_selector_tree.yx \
	ywait_send_comment.yx \
	ywait_send_message.yx \
	ywait_send_password.yx \
	ywait_table.yx \
	ywait_test_plugin.yx \
	ywait_viewer.yx \
	ywait_viewer_append.yx \
	ywait_viewer_help.yx

ALLFILES = $(SOURCE:._YX=.yx)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/screens

