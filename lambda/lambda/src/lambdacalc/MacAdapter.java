package lambdacalc;

import javax.swing.JOptionPane;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;


public class MacAdapter extends ApplicationAdapter {
  private Main handler;
  public MacAdapter(Main handler) {
    this.handler = handler;
  }
  public void handleQuit(ApplicationEvent e) {
    System.exit(0);
  }
  public void handleAbout(ApplicationEvent e) {
    // tell the system we're handling this, so it won't display
    // the default system "about" dialog after ours is shown.
    e.setHandled(true);
    JOptionPane.showMessageDialog(null,
            "<html><b>Lambda Calculator</b></html>\n" +
            "Version 1.2\n" +
            "Developed at The University of Pennsylvania and New York University\n"
            + "by Lucas Champollion, Joshua Tauberer,  Maribel Romero, and Dylan Bumford",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
  }
  public void handlePreferences(ApplicationEvent e) {
    JOptionPane.showMessageDialog(null, "Show Preferences dialog here");
  }
}
