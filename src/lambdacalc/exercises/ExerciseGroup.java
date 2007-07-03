/*
 * ExerciseGroup.java
 *
 * Created on May 31, 2006, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

import java.util.ArrayList;

/**
 * Represents a titled subsection in an exercise file, typically containing
 * exercises of the same type.
 */
public class ExerciseGroup {
    
    private String title, directions;
    private ArrayList items = new ArrayList();
    private int index;
    
    ExerciseGroup(int index) {
        title = "Exercise Group";
        directions = "";
        this.index = index;
    }
    
    public String toString() {
        return getTitle();
    }
    
    public String getNumberedTitle() {
            return "Part " + (char)((int)'A' + getIndex()) + ". " + getTitle();
    }
    
    /**
     * Gets the title of the exercise group.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title of the ExerciseGroup. The title may not be either
     * null or the empty string.
     */
    public void setTitle(String title) {
        if (title == null || title.equals(""))
            throw new IllegalArgumentException();
        this.title = title;
    }
    
    /**
     * Gets the directions to the student for the group. May be null.
     */
    public String getDirections() {
        return directions;
    }
    
    /**
     * Sets the directions to the student for the group.
     */
    public void setDirections(String directions) {
        this.directions = directions;
    }
    
    /**
     * Gets the index of this group in the ExerciseFile that contains it.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Adds an Exercise to the end of this group.
     */
    public void addItem(Exercise item) {
        items.add(item);
    }
    
    /**
     * Returns the number of exercises in this group.
     */
    public int size() {
        return items.size();
    }
    
    /**
     * Gets the exercise at the given index in this group.
     */
    public Exercise getItem(int index) {
        return (Exercise)items.get(index);
    }
    
    /**
     * Serializes the group to a stream.
     */
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(1); // just some versioning for future use
        output.writeUTF(title);
        output.writeUTF(directions);
        output.writeShort(size());
        for (int i = 0; i < size(); i++) {
            Exercise e = getItem(i);
            if (e instanceof TypeExercise)
                output.writeShort(1);
            else if (e instanceof LambdaConversionExercise)
                output.writeShort(2);
            else if (e instanceof TreeExercise)
                output.writeShort(3);
            else
                throw new RuntimeException("Exercise type not recognized in ExerciseGroup::WriteToStream.");
            e.writeToStream(output);
            output.writeBoolean(e.isDone());
            output.writeUTF(e.getPoints().toString());
            output.writeBoolean(e.getInstructions() != null);
            if (e.getInstructions() != null)
                output.writeUTF(e.getInstructions());
        }
    }
    
    /**
     * Initializes this instance with the serialized group data from a stream.
     */
    public void readFromStream(java.io.DataInputStream input, int fileFormatVersion) throws java.io.IOException, ExerciseFileFormatException {
        if (input.readShort() != 1) throw new ExerciseFileVersionException();
        
        title = input.readUTF();
        directions = input.readUTF();
        int nEx = input.readShort();
        for (int i = 0; i < nEx; i++) {
            int exType = input.readShort();
            
            Exercise ex;
            if (exType == 1)
                ex = new TypeExercise(input, fileFormatVersion, i);
            else if (exType == 2)
                ex = new LambdaConversionExercise(input, fileFormatVersion, i);
            else if (exType == 3)
                ex = new TreeExercise(input, fileFormatVersion, i);
            else
                throw new ExerciseFileFormatException();
            
            items.add(ex);
            
            if (input.readBoolean())
                ex.setDone();
            
            ex.setPoints(new java.math.BigDecimal(input.readUTF()));
            
            if (input.readBoolean())
                ex.setInstructions(input.readUTF());
            else
                ex.setInstructions(null);
        }
    }
}
