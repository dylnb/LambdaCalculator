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
public class ExerciseFile {
    
    String title;
    ArrayList groups = new ArrayList();
    
    String studentName;
    
    String teacherComments;
    
    /**
     * Creates a new ExerciseFile.
     */
    public ExerciseFile() {
        title = "Exercises";
    }
    
    public String toString() {
        return getTitle();
    }

    /**
     * Appends a new ExerciseGroup to the end of this exercise file and returns the group.
     */
    public ExerciseGroup addGroup() {
        ExerciseGroup group = new ExerciseGroup(groups.size());
        groups.add(group);
        return group;
    }
    
    /**
     * Gets the number of ExerciseGroups in the file.
     */
    public int size() {
        return groups.size();
    }
    
    /**
     * Gets the ExerciseGroup at the given index.
     */
    public ExerciseGroup getGroup(int index) {
        return (ExerciseGroup)groups.get(index);
    }
    
    /**
     * Gets the title of the ExerciseFile.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title of the ExerciseFile. The title may not be either
     * null or the empty string.
     */
    public void setTitle(String title) {
        if (title == null || title.equals(""))
            throw new IllegalArgumentException();
        this.title = title;
    }
    
    /**
     * Gets the student name associated with the file. May be null if no
     * student name is associated with the file.
     */
    public String getStudentName() {
        return studentName;
    }
    
    /**
     * Sets the student name associated with the file.
     * May be null to clear the name associated with the file.
     * @throws IllegalArgumentException if name is an empty
     * string or contains only spaces.
     */
    public void setStudentName(String name) {
        if (name != null && name.trim().length() == 0)
            throw new IllegalArgumentException();
        this.studentName = name;
    }

    /**
     * Gets teacher-written comments in the file. May be null
     * if no comments have been entered
     */
    public String getTeacherComments() {
        return teacherComments;
    }
    
    /**
     * Sets teacher comments into the file. May be null to
     * clear the comments.
     */
    public void setTeacherComments(String comments) {
        this.teacherComments = comments;
    }

    /**
     * Gets whether any exercise in the file has been started.
     * (For single-step problems, this would be if any exercise has been completed,
     * but for multiple-step problems, this is if the user has given any correct
     * intermediate answers.)
     */
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

    /**
     * Gets whether every exercise in the file has been successfully completed.
     */
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
    
    
    /**
     * Gets a list of all of the exercises in the file.
     */
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

    /**
     * Saves the exercises in seralized form to the given file.
     */
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
        
        if (teacherComments == null) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            output.writeUTF(teacherComments);
        }

        output.writeShort(groups.size());

        for (int i = 0; i < size(); i++) {
            ExerciseGroup g = getGroup(i);
            g.writeToStream(output);
        }
        
        output.close();
    }
    
    /**
     * Reads the serialized ExerciseFile data from the given file and initializes this
     * instance with the serialized data.
     */
    public ExerciseFile(File source) throws IOException, ExerciseFileFormatException {
        InputStream stream = new FileInputStream(source);
        DataInputStream input = new DataInputStream(stream);
        
        if (input.readByte() != 'L') throw new ExerciseFileFormatException();
        if (input.readByte() != 'A') throw new ExerciseFileFormatException();
        if (input.readByte() != 'M') throw new ExerciseFileFormatException();
        if (input.readByte() != 'B') throw new ExerciseFileFormatException();
        if (input.readByte() != 'D') throw new ExerciseFileFormatException();
        if (input.readByte() != 'A') throw new ExerciseFileFormatException();
        
        short formatVersion = input.readShort();
        if (formatVersion != 1) throw new ExerciseFileVersionException();
        
        input = new DataInputStream(new java.util.zip.InflaterInputStream(input));

        title = input.readUTF();
        
        if (input.readByte() == 1) // otherwise the byte is zero an studentName is null
            studentName = input.readUTF();
        
        if (input.readByte() == 1) // otherwise the byte is zero an studentName is null
            teacherComments = input.readUTF();
 
        int nGroups = input.readShort();
        
        for (int i = 0; i < nGroups; i++) {
            ExerciseGroup g = addGroup();
            g.readFromStream(input, formatVersion);
        }
        
        input.close();
    }
}
