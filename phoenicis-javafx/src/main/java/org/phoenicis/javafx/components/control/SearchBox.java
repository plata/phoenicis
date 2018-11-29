package org.phoenicis.javafx.components.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.phoenicis.javafx.components.skin.SearchBoxSkin;

import java.util.function.Consumer;

/**
 * A search box component used to add a search term
 */
public class SearchBox extends ControlBase<SearchBox, SearchBoxSkin> {
    /**
     * A consumer, which is called when the search term has been modified
     */
    private final ObjectProperty<Consumer<String>> onSearch;

    /**
     * A consumer, which is called when the clear button has been pressed
     */
    private final ObjectProperty<Runnable> onClear;

    /**
     * Constructor
     *
     * @param onSearch callback for search input
     * @param onClear callback for clear input
     */
    public SearchBox(ObjectProperty<Consumer<String>> onSearch, ObjectProperty<Runnable> onClear) {
        super();

        this.onSearch = onSearch;
        this.onClear = onClear;
    }

    /**
     * Constructor
     *
     * @param onSearch callback for search input
     * @param onClear callback for clear input
     */
    public SearchBox(Consumer<String> onSearch, Runnable onClear) {
        this(new SimpleObjectProperty<>(onSearch), new SimpleObjectProperty<>(onClear));
    }

    /**
     * Constructor
     */
    public SearchBox() {
        this(new SimpleObjectProperty<>(), new SimpleObjectProperty<>());
    }

    /**
     * {@inheritDoc}
     *
     * @return A created search box skin object
     */
    @Override
    public SearchBoxSkin createSkin() {
        return new SearchBoxSkin(this);
    }

    public ObjectProperty<Consumer<String>> onSearchProperty() {
        return onSearch;
    }

    public Consumer<String> getOnSearch() {
        return onSearch.get();
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch.set(onSearch);
    }

    public ObjectProperty<Runnable> onClearProperty() {
        return onClear;
    }

    public Runnable getOnClear() {
        return onClear.get();
    }

    public void setOnClear(Runnable onClear) {
        this.onClear.set(onClear);
    }
}