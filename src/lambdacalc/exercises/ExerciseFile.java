/*
 * ExerciseFile.java
 *
 * Created on May 31, 2006, 11:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

import java.io.*;
import java.util.*;
import java.util.zip.DataFormatException;
import lambdacalc.logic.*;

/**
 * Represents a set of Exercises grouped into one or more ExerciseGroups.
 * The teacher creates a text file using a text which is read into an ExerciseFile
 * via an ExerciseFileParser.
 */
public class ExerciseFile implements Serializable {
    
    String title;
    ArrayList groups = new ArrayList();
    
    String studentName;
    
    public ExerciseFile() {
        title = "Exercises";
    }
    
    public String toString() {
        return getTitle();
    }

    public void addGroup(ExerciseGroup group) {
        groups.add(group);
    }
    
    public int size() {
        return groups.size();
    }
    
    public ExerciseGroup getGroup(int index) {
        return (ExerciseGroup)groups.get(index);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String title) {
        this.studentName = title;
    }

    public boolean hasBeenStarted() {
        for (int i = 0; i < size(); i++) {
            ExerciseGroup g = getGroup(i);
            for (int j = 0; j < g.size(); j++) {
                Exercise e = g.getItem(j);
                if (e.hasBeenStarted())
                    return true;
            }
        }
        return false;
    }

    public boolean hasBeenCompleted() {
        for (int i = 0; i < size(); i++) {
            ExerciseGroup g = getGroup(i);
            for (int j = 0; j < g.size(); j++) {
                Exercise e = g.getItem(j);
                if (!e.isDone())
                    return false;
            }
        }
        return true;
    }
    
    
    public List exercises() { 
        List l = new Vector();
        for (int i = 0; i < size(); i++) {
            ExerciseGroup g = getGroup(i);
            for (int j = 0; j < g.size(); j++) {
                Exercise e = g.getItem(j);
                l.add(e);
            }
        }
        return l;
    }
   
    /**
     * Returns the total number of exercises completed correctly.
     */
    public int getNumberCorrect() {
        Iterator iter = exercises().iterator();
        int count = 0;
        while (iter.hasNext()) {
            Exercise e = (Exercise) iter.next();
            if (e.isDone())
                count++;
        }
        return count;
    }
    
    /**
     * Returns the total amount of points that could be awarded
     * in this problem set (i.e. 100, if the points add up).
     */
    public java.math.BigDecimal getTotalPointsAvailable() {
        Iterator iter = exercises().iterator();
        java.math.BigDecimal ret = java.math.BigDecimal.valueOf(0);
        while (iter.hasNext()) {
            Exercise e = (Exercise) iter.next();
            ret = ret.add(e.getPoints());
        }
        return ret;
    }

    /**
     * Returns the amount of points the student has achieved
     * in his correct answers.
     */
    public java.math.BigDecimal getPointsCorrect() {
        Iterator iter = exercises().iterator();
        java.math.BigDecimal ret = java.math.BigDecimal.valueOf(0);
        while (iter.hasNext()) {
            Exercise e = (Exercise) iter.next();
            if (e.isDone())
                ret = ret.add(e.getPoints());
        }
        return ret;
    }

    public void saveTo(File target) throws IOException {
        OutputStream stream = new FileOutputStream(target);
        
        DataOutputStream output = new DataOutputStream(stream);
        output.writeBytes("LAMBDA"); // magic string
        output.writeShort(1);        // file version format number
        output.flush();
        
        output = new DataOutputStream( new java.util.zip.DeflaterOutputStream(stream));

        output.writeUTF(title);
        if (studentName == null) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            output.writeUTF(studentName);
        }
        
        output.writeShort(groups.size());

        for (int i = 0; i < size(); i++) {
            ExerciseGroup g = getGroup(i);
            g.writeToStream(output);
        }
        
        output.close();
    }
    
    public void readFrom(File source) throws IOException, ExerciseFileFormatException {
        InputStream stream = new FileInputStream(source);
        DataInputStream input = new DataInputStream(stream);
        
        if (input.readByte() != 'L') throw new ExerciseFileFormatException();
        if (input.readByte() != 'A') throw new ExerciseFileFormatException();
        if (input.readByte() != 'M') throw new ExerciseFileFormatException();
        if (input.readByte() != 'B') throw new ExerciseFileFormatException();
        if (input.readByte() != 'D') throw new ExerciseFileFormatException();
        if (input.readByte() != 'A') throw new ExerciseFileFormatException();
        
        short formatVersion = input.readShort();
        if (formatVersion != 1) throw new ExerciseFileFormatException("This file was saved with a future version of the Lambda program.");
        
        input = new DataInputStream(new java.util.zip.InflaterInputStream(input));

        title = input.readUTF();
        
        if (input.readByte() == 1)
            studentName = input.readUTF();
        
        int nGroups = input.readShort();
        
        for (int i = 0; i < nGroups; i++) {
            ExerciseGroup g = new ExerciseGroup(i);
            g.readFromStream(input, formatVersion);
            groups.add(g);
        }
        
        input.close();
    }
}
