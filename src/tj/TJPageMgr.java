package tj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class TJPageMgr implements Serializable {
    // constants
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME = "journal.dat"; // file name
    public static final double WORLD_PAGE_WIDTH = 900;
    public static final double WORLD_PAGE_HEIGHT = 1200;
    
    // fields
    private TJ mTJ = null;
    // list of journal entries/pages (TJPage[0] = Left Page, TJPage[1] = Right Page)
    private ArrayList<TJPage[]> mJournalPages;
    private int mCurPageIndex; // index of the currently displayed journal page

    public TJPageMgr(TJ tj) {
        this.mTJ = tj;
        this.mJournalPages = new ArrayList<>();
        this.mCurPageIndex = 0;
    }
    
    public ArrayList<TJPage[]> getJournalPages() {
        return this.mJournalPages;
    }
    
    public int getCurPageIndex() {
        return this.mCurPageIndex;
    }
    
    public void setCurPageIndex(int index) {
        if (index >= 0 && index < mJournalPages.size()) {
            this.mCurPageIndex = index;
            System.out.println("now we are on page " + this.mCurPageIndex);
            this.mTJ.getCanvas2D().repaint();
        }
    }
    
    public TJPage[] getCurPage() {
        if (this.mJournalPages.isEmpty() || this.mCurPageIndex < 0 ||
            this.mCurPageIndex >= this.mJournalPages.size()) {
            return null;
        }
        
        return this.mJournalPages.get(this.mCurPageIndex);
    }
    
    public TJPage[] getPrevPage() {
        if (this.mCurPageIndex > 0) {
            return this.mJournalPages.get(this.mCurPageIndex - 1);
        }
        
        return new TJPage[] { TJPage.BLANK_HIDDEN_PAGE, TJPage.BLANK_HIDDEN_PAGE };
    }
    
    public TJPage[] getNextPage() {
        if (this.mCurPageIndex < this.mJournalPages.size() - 1) {
            return this.mJournalPages.get(this.mCurPageIndex + 1);
        }
        
        return new TJPage[] { TJPage.BLANK_HIDDEN_PAGE, TJPage.BLANK_HIDDEN_PAGE };
    }

    public void addEmptyPage() {
        TJPage leftPage = new TJPage();
        TJPage rightPage = new TJPage();
        
        int insertionIndex;
        
        if (this.mJournalPages.isEmpty()) {
            insertionIndex = 0;
            this.mJournalPages.add(new TJPage[] {leftPage, rightPage});
        } else {
             // insert at the position after the current one
             insertionIndex = this.mCurPageIndex + 1;
             this.mJournalPages.add(insertionIndex, new TJPage[] {leftPage, rightPage});
        }
        
        // update the index to point to the newly inserted spread
        this.mCurPageIndex = insertionIndex;
        this.mTJ.getCanvas2D().repaint();
        System.out.println("🆕 a page is added, now total page = " + this.mJournalPages.size());
    }
    
    public void deleteCurPage() {
        if (this.mJournalPages.size() > 1) {
            int idx = this.mCurPageIndex;
            this.mJournalPages.remove(idx);
            if (idx >= this.mJournalPages.size()) {
                this.mCurPageIndex = this.mJournalPages.size() - 1;
            }
            this.mTJ.getCanvas2D().repaint();
        } else if (this.mJournalPages.size() == 1) {
            // Only one page left, reset it to blank
            this.mJournalPages.set(0, new TJPage[] { new TJPage(), new TJPage() });
            this.mCurPageIndex = 0;
            this.mTJ.getCanvas2D().repaint();
        }
        System.out.println("🗑 a page is deleted, now total page = " + this.mJournalPages.size());
    }

    public void saveJournal() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(this.mJournalPages);
            System.out.println("Journal saved successfully to " + FILE_NAME);
        } catch (Exception e) {
            System.err.println("Error saving journal: " + e.getMessage());
        }
    }

    public boolean loadJournal() throws FileNotFoundException, IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No existing journal data found. Starting fresh.");
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // read the object and check its type before casting
            Object loadedObject = ois.readObject();
            
            if (loadedObject instanceof ArrayList) {
                this.mJournalPages = (ArrayList<TJPage[]>) loadedObject;
                this.mCurPageIndex = 0; // reset to the first page upon loading
                System.out.println("Journal loaded successfully. Total pages: " + this.mJournalPages.size());
                return true;
            } else {
                 System.err.println("Error loading journal: File content is corrupted or not a valid journal format");
                 this.mJournalPages = new ArrayList<>();
                 return false;
            }

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error loading journal: " + e.getMessage());
            // if loading fails due to IO or class mismatch, start fresh
            this.mJournalPages = new ArrayList<>();
            return false;
        }
    }
}