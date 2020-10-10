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

package att.research.yoix.j3d;
import javax.media.j3d.*;
import att.research.yoix.*;

public
class J3DAlpha extends Alpha
{
    private long  resumetime;
    private boolean dead_mode = false;

    public J3DAlpha() {
	super();

	resumetime = getStartTime();
    }

    public long
    getResumeTime() {
	return resumetime;
    }

    public synchronized boolean
    isAlive() {
	return !(
	    dead_mode
	    ||
	    finished()
	    ||
	    System.currentTimeMillis() < (getStartTime() + getTriggerTime())
	    );
    }

    public void
    pause() {
	pause(System.currentTimeMillis());
    }

    public synchronized void
    pause(long ptime) {
	if (!isPaused() && isAlive()) {
	    if (ptime > 0)
		super.pause(ptime);
	}
    }

    public void
    resume() {
	resume(System.currentTimeMillis());
    }

    public synchronized void
    resume(long rtime) {
	if (isPaused() && !dead_mode) {
	    if (rtime > 0) {
		resumetime = rtime;
		super.resume(resumetime);
	    }
	}
    }

    public synchronized void
    setAlive(boolean mode) {
	if (mode) {
	    if (!isAlive()) {
		dead_mode = false;
		resume(getPauseTime());
	    }
	} else {
	    pause(getStartTime());
	    dead_mode = true;
	}
    }
}
