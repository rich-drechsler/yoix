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
import java.awt.*;
import java.awt.event.*;

class YoixAWTErrorDialog extends Dialog

    implements ActionListener

{

    //
    // Simple standalone Dialog that can be used to display error
    // messages. May not be used, so it could disappear.
    //

    private Toolkit  toolkit = Toolkit.getDefaultToolkit();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTErrorDialog(String details[], boolean modal) {

	super((Frame)new Frame(), modal);
	buildDialog(details);
    }

    ///////////////////////////////////
    //
    // ActionListener Methods
    //
    ///////////////////////////////////

    public void
    actionPerformed(ActionEvent e) {

	setVisible(false);
	dispose();
    }

    ///////////////////////////////////
    //
    // YoixInstallerDialog Methods
    //
    ///////////////////////////////////

    public void
    show() {

	Rectangle  rect;
	Dimension  screensize = toolkit.getScreenSize();
	int        resolution = toolkit.getScreenResolution();

	pack();
	rect = getBounds();
	rect.width += resolution/2;
	rect.height += resolution/4;
	rect.width = Math.max(rect.width, (int)(6.5*resolution));
	rect.height = Math.max(rect.height, (int)(1.5*resolution));
	rect.x = (screensize.width - rect.width)/2;
	rect.y = (screensize.height - rect.height)/2;
	setBounds(rect);
	toolkit.beep();
	super.show();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildDialog(String details[]) {

	LayoutManager  manager;
	TextArea       textarea;
	String         text = "";
	Button         button;
	Panel          buttonpanel;
	Label          label;
	int            resolution;
	int            rows;
	int            columns;

	if (details.length > 0) {
	    resolution = toolkit.getScreenResolution();
	    setLayout(new BorderLayout(resolution/4, resolution/16));

	    label = new Label(details[0], Label.CENTER);
	    label.setFont(new Font("Helvetica", Font.BOLD, 14));
	    label.setForeground(Color.red);

	    for (rows = 1, columns = 70; rows < details.length; rows++) {
		columns = Math.max(columns, details[rows].length() + 1);
		text += details[rows];
	    }

	    textarea = new TextArea(text, rows, columns, TextArea.SCROLLBARS_NONE);
	    textarea.setFont(new Font("Courier", Font.BOLD, 12));

	    buttonpanel = new Panel();
	    button = new Button("Dismiss");
	    button.addActionListener(this);
	    buttonpanel.add(button);

	    add(label, BorderLayout.NORTH);
	    add(textarea, BorderLayout.CENTER);
	    add(buttonpanel, BorderLayout.SOUTH);
	    add(new Canvas(), BorderLayout.EAST);
	    add(new Canvas(), BorderLayout.WEST);
	    setBackground(Color.lightGray);
	}
    }
}

