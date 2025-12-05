package tj;

import java.io.Serializable;
import java.util.ArrayList;

public class TJPage implements Serializable {
    // unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    
    // constants
    public static final TJPage BLANK_HIDDEN_PAGE = new TJPage(false);
    
    // fields: what goes on the journal page
    private ArrayList<TJPenMark> mPenMarks;
    public ArrayList<TJPenMark> getPenMarks() {
        return this.mPenMarks;
    }
    
    private boolean mIsEditable = true;
    
    // private constructor
    private TJPage(boolean isEditable) {
        this.mPenMarks = new ArrayList<>();
        this.mIsEditable = isEditable;
    }
    // public constructor to create editable pages
    public TJPage() {
        this(true);
    }
    
    public boolean isBlankHiddenPage() {
        return this == BLANK_HIDDEN_PAGE;
    }
    
    public boolean isContentEmpty() {
        // TODO: update this as the functionality increase
        return this.mPenMarks.isEmpty();
    }
}
