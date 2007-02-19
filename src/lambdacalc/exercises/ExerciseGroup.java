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
    
    String title, directions;
    ArrayList items = new ArrayList();
    int index;
    
    public ExerciseGroup(int index) {
        title = "Exercise Group";
        directions = "";
        this.index = index;
    }
    
    public String toString() {
        return getTitle();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDirections() {
        return directions;
    }
    
    public void setDirections(String directions) {
        this.directions = directions;
    }
    
    public int getIndex() {
        return index;
    }

    public void addItem(Exercise item) {
        items.add(item);
    }
    
    public int size() {
        return items.size();
    }
    
    public Exercise getItem(int index) {
        return (Exercise)items.get(index);
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(0); // just some versioning for future use
        output.writeUTF(title);
        output.writeUTF(directions);
        output.writeShort(size());
        for (int i = 0; i < size(); i++) {
            Exercise e = getItem(i);
            if (e instanceof TypeExercise)
                output.writeShort(1);
            else if (e instanceof LambdaConversionExercise)
                output.writeShort(2);
            else
                throw new RuntimeException("Exercise type not recognized in ExerciseGroup::WriteToStream.");
            e.writeToStream(output);
            output.writeBoolean(e.isDone());
        }
    }
    
    public void readFromStream(java.io.DataInputStream input, int fileFormatVersion) throws java.io.IOException, ExerciseFileFormatException {
        if (input.readShort() != 0) throw new ExerciseFileFormatException();
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
            else
                throw new ExerciseFileFormatException();
            
            items.add(ex);
            
            if (input.readBoolean())
                ex.setDone();
        }
    }
}
