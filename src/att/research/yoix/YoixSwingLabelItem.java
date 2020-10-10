/*
 *  This software may only be used by you under license from AT&T Corp.
 *  ("AT&T").  A copy of AT&T's Source Code Agreement is available at
 *  AT&T's Internet website having the URL:
 *
 *    <http://www.research.att.com/sw/tools/yoix/license/source.html>
 *
 *  If you received this software without first entering into a license
 *  with AT&T, you have an infringing copy of this software and cannot
 *  use it without violating AT&T's intellectual property rights.
 */

package att.research.yoix;
import javax.swing.Icon;

class YoixSwingLabelItem {

    //
    // Convenience class to keep label, mapping (or command) and icon
    // information together in one place for things like list items, etc.
    //

    private String  text;
    private String  mapping;
    private Icon    icon;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingLabelItem() {

	this(null, null, null);
    }


    YoixSwingLabelItem(String text) {

	this(null, text, null);
    }


    YoixSwingLabelItem(String text, String mapping) {

	this(null, text, mapping);
    }


    YoixSwingLabelItem(Icon icon, String text, String mapping) {

	this.icon = icon;
	this.text = text;
	this.mapping = mapping;
    }

    ///////////////////////////////////
    //
    // YoixSwingLabelItem Methods
    //
    ///////////////////////////////////

    final Icon
    getIcon() {

	return(icon);
    }


    final String
    getMapping() {

	return(mapping);
    }


    final String
    getText() {

	return(text);
    }


    final String
    getValue() {

	return(mapping == null ? text : mapping);
    }


    final void
    setIcon(Icon icon) {

	this.icon = icon;
    }


    final void
    setMapping(String mapping) {

	this.mapping = mapping;
    }


    final void
    setText(String text) {

	this.text = text;
    }


    public final String
    toString() {

	return(text == null ? "" : text);
    }
}

