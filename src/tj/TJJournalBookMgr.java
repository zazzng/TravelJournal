package tj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.awt.geom.Point2D;

public class TJJournalBookMgr implements Serializable {
    // constants
    private static final long serialVersionUID = 1L;
    // main file to store the list of all books (metadata only)
    private static final String METADATA_FILE_NAME = "journal_index.dat"; 
    private static final String JOURNAL_DIR = "journal"; // directory to hold individual book files
    public static final double WORLD_PAGE_WIDTH = 900;
    public static final double WORLD_PAGE_HEIGHT = 1200;
    
    // fields
    private TJ mTJ = null;
    
    // metadata (title, pinpoint) for ALL books
    private ArrayList<TJBookMetadata> mBookMetadata; 
    
    // the content (pages) of the CURRENTLY SELECTED book
    private transient TJJournalBook mCurrentBookCache = null; 
    
    private int mCurBookIndex; 
    private int mCurPageIndex; 

    public TJJournalBookMgr(TJ tj) {
        this.mTJ = tj;
        this.mBookMetadata = new ArrayList<>();
        this.mCurBookIndex = -1;
        this.mCurPageIndex = 0;
        new File(JOURNAL_DIR).mkdirs();
    }
    
    public ArrayList<TJBookMetadata> getBookMetadata() {
        return this.mBookMetadata;
    }
    
    public TJJournalBook getCurBook() {
        if (this.mCurBookIndex < 0 || this.mCurBookIndex >= this.mBookMetadata.size()) {
            return null;
        }
        
        if (mCurrentBookCache == null || 
            !mCurrentBookCache.getTitle().equals(mBookMetadata.
            get(mCurBookIndex).title)) {
            loadBookContent(mCurBookIndex);
        }
        
        return mCurrentBookCache;
    }
    
    public void addNewBook(String title, Point2D.Double pinPoint) {
        TJJournalBook newBook = new TJJournalBook(title, pinPoint);
        mBookMetadata.add(new TJBookMetadata(newBook.getTitle(), newBook.getPinPoint()));
        
        this.mCurrentBookCache = newBook;
        this.mCurBookIndex = this.mBookMetadata.size() - 1; 
        this.mCurPageIndex = 0; 
        
        System.out.println("@JournalBookMgr: curPageIndex = " + this.mCurPageIndex);

        saveSingleBook(newBook);
        saveMetadataIndex();
        
        this.mTJ.getCanvas2D().repaint();
        System.out.println("🆕 New book added: " + newBook.getTitle());
    }
    
    public void selectBook(int bookIndex) {
        if (bookIndex >= 0 && bookIndex < mBookMetadata.size()) {
            this.mCurBookIndex = bookIndex;
            this.mCurPageIndex = 0; 
            
            loadBookContent(bookIndex); 
            this.mTJ.getCanvas2D().repaint();
            System.out.println("Book selected: " + getCurBook().getTitle());
        }
    }
    
    public boolean findBookByPinPoint(Point2D.Double requestPoint, double tolerance) {
        if (requestPoint == null) return false;

        for (int i = 0; i < this.mBookMetadata.size(); i++) {
            TJBookMetadata metadata = this.mBookMetadata.get(i);
            Point2D.Double bookPin = metadata.pinPoint;

            if (bookPin != null) {
                if (bookPin.distanceSq(requestPoint) <= (tolerance * tolerance)) {
                    selectBook(i);
                    return true;
                }
            }
        }

        System.out.println("❌ No journal book found at the requested pin-point.");
        return false;
    }
    
    private boolean loadBookContent(int bookIndex) {
        if (bookIndex < 0 || bookIndex >= mBookMetadata.size()) return false;
        
        TJBookMetadata metadata = mBookMetadata.get(bookIndex);
        
        String sanitizedTitle = metadata.title.replaceAll("[^a-zA-Z0-9.-]", "_");
        String filePath = JOURNAL_DIR + "/" + sanitizedTitle + ".dat";
        File file = new File(filePath);
        
        if (!file.exists()) {
            System.err.println("Error: Book content file not found for: " + metadata.title);
            mCurrentBookCache = null;
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loadedObject = ois.readObject();
            
            if (loadedObject instanceof TJJournalBook) {
                this.mCurrentBookCache = (TJJournalBook) loadedObject;
                this.mCurBookIndex = bookIndex;
                System.out.println("📖 Loaded content for book: " + metadata.title);
                return true;
            } else {
                 System.err.println("Error loading book content: Invalid format for " + metadata.title);
                 mCurrentBookCache = null;
                 return false;
            }

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error loading book content (" + metadata.title + "): " + e.getMessage());
            mCurrentBookCache = null;
            return false;
        }
    }
    
    public boolean deleteBookByTitle(String title) {
        if (title == null) return false;

        String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9.-]", "_");
        File bookFile = new File(JOURNAL_DIR + "/" + sanitizedTitle + ".dat");

        int indexToRemove = -1;
        for (int i = 0; i < this.mBookMetadata.size(); i++) {
            if (this.mBookMetadata.get(i).title.equals(title)) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove != -1) {
            this.mBookMetadata.remove(indexToRemove);

            boolean fileDeleted = bookFile.delete();

            if (mCurrentBookCache != null && mCurrentBookCache.getTitle().equals(title)) {
                mCurrentBookCache = null;
            }
            
            if (mCurBookIndex >= indexToRemove) {
                mCurBookIndex--;
            }

            if (mBookMetadata.isEmpty()) {
                mCurBookIndex = -1;
                mCurPageIndex = 0;
            } else if (mCurBookIndex < 0) {
                mCurBookIndex = 0;
            }

            saveMetadataIndex();
            return true;
        }

        return false;
    }
    
    private ArrayList<TJPage[]> getCurBookPages() {
        TJJournalBook book = getCurBook();
        return (book != null) ? book.getPages() : new ArrayList<>();
    }
    
    public int getCurPageIndex() {
        return this.mCurPageIndex;
    }
    
    public void setCurPageIndex(int index) {
        ArrayList<TJPage[]> pages = getCurBookPages();
        if (index >= 0 && index < pages.size()) {
            this.mCurPageIndex = index;
            System.out.println("now we are on page " + this.mCurPageIndex);
            this.mTJ.getCanvas2D().repaint();
        }
    }
    
    public TJPage[] getCurPage() {
        ArrayList<TJPage[]> pages = getCurBookPages();
        if (pages.isEmpty() || this.mCurPageIndex < 0 ||
            this.mCurPageIndex >= pages.size()) {
            return null;
        }
        return pages.get(this.mCurPageIndex);
    }
    
    public TJPage[] getPrevPage() {
        ArrayList<TJPage[]> pages = getCurBookPages();
        if (this.mCurPageIndex > 0) {
            return pages.get(this.mCurPageIndex - 1);
        }
        return new TJPage[] { TJPage.BLANK_HIDDEN_PAGE, TJPage.BLANK_HIDDEN_PAGE };
    }
    
    public TJPage[] getNextPage() {
        ArrayList<TJPage[]> pages = getCurBookPages();
        if (this.mCurPageIndex < pages.size() - 1) {
            return pages.get(this.mCurPageIndex + 1);
        }
        return new TJPage[] { TJPage.BLANK_HIDDEN_PAGE, TJPage.BLANK_HIDDEN_PAGE };
    }

    public void addEmptyPage() {
        TJJournalBook book = getCurBook();
        if (book == null) {
            System.err.println("Cannot add page: No book is currently selected.");
            return;
        }
        
        ArrayList<TJPage[]> pages = book.getPages();
        TJPage leftPage = new TJPage();
        TJPage rightPage = new TJPage();
        
        int insertionIndex = this.mCurPageIndex + 1;
        
        if (pages.isEmpty()) { 
            insertionIndex = 0;
            pages.add(new TJPage[] {leftPage, rightPage});
        } else {
             pages.add(insertionIndex, new TJPage[] {leftPage, rightPage});
        }
        
        this.mCurPageIndex = insertionIndex;
        this.mTJ.getCanvas2D().repaint();
        System.out.println("🆕 a page is added, now total pages in book = " + pages.size());
    }
    
    public void deleteCurPage() {
        TJJournalBook book = getCurBook();
        if (book == null) return;
        
        ArrayList<TJPage[]> pages = book.getPages();
        
        if (pages.size() > 1) {
            int idx = this.mCurPageIndex;
            pages.remove(idx);
            if (idx >= pages.size()) {
                this.mCurPageIndex = pages.size() - 1;
            }
            this.mTJ.getCanvas2D().repaint();
            System.out.println("🗑 a page is deleted, now total pages in book = " + pages.size());
        } else if (pages.size() == 1) {
            pages.set(0, new TJPage[] { new TJPage(), new TJPage() });
            this.mCurPageIndex = 0;
            this.mTJ.getCanvas2D().repaint();
            System.out.println("🗑 Last page reset to blank.");
        }
    }

    public void saveJournal() {
        if (mCurrentBookCache != null) {
            saveSingleBook(mCurrentBookCache);
        }
        
        saveMetadataIndex();
        System.out.println("✅ Journal state saved successfully (Index + cached book).");
    }
    
    private void saveSingleBook(TJJournalBook book) {
        String sanitizedTitle = book.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
        String filePath = JOURNAL_DIR + "/" + sanitizedTitle + ".dat";
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(book); // Write the entire TJJournalBook object
            System.out.println("💾 Saved book content: " + book.getTitle());
        } catch (Exception e) {
            System.err.println("Error saving book content (" + book.getTitle() + "): " + e.getMessage());
        }
    }

    private void saveMetadataIndex() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(METADATA_FILE_NAME))) {
            oos.writeObject(this.mBookMetadata);
        } catch (Exception e) {
            System.err.println("Error saving metadata index: " + e.getMessage());
        }
    }

    public boolean loadJournal() {
        File file = new File(METADATA_FILE_NAME);
        if (!file.exists()) {
            System.out.println("No existing journal data found. Starting fresh.");
            this.mBookMetadata = new ArrayList<>();
            this.mCurBookIndex = -1;
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loadedObject = ois.readObject();
            
            if (loadedObject instanceof ArrayList) {
                this.mBookMetadata = (ArrayList<TJBookMetadata>) loadedObject;
                
                if (!this.mBookMetadata.isEmpty()) {
                    this.mCurBookIndex = 0; 
                    this.mCurPageIndex = 0; 
                    // Content is NOT loaded here; it will be loaded by getCurBook() on demand.
                    System.out.println("✅ Journal metadata loaded. Total books: " + this.mBookMetadata.size());
                } else {
                    this.mCurBookIndex = -1;
                }
                return true;
            } else {
                 System.err.println("Error loading journal: Invalid metadata format.");
                 this.mBookMetadata = new ArrayList<>();
                 this.mCurBookIndex = -1;
                 return false;
            }

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error loading journal metadata: " + e.getMessage());
            this.mBookMetadata = new ArrayList<>();
            this.mCurBookIndex = -1;
            return false;
        }
    }
}