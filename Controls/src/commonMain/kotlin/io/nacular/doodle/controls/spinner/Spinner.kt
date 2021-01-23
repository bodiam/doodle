package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList


public interface Model<T> {
    public fun next    ()
    public fun previous()

    public val value      : T
    public val hasNext    : Boolean
    public val hasPrevious: Boolean
    public val changed    : ChangeObservers<Model<T>>
}

public interface MutableModel<T>: Model<T> {
    public override var value: T
}

/**
 * Provides presentation and behavior customization for [Spinner].
 */
public abstract class SpinnerBehavior<T, M: Model<T>>: Behavior<Spinner<T, M>> {
    public val Spinner<T, M>.children: ObservableList<View> get() = this._children
    public var Spinner<T, M>.insets  : Insets               get() = this._insets; set(new) { _insets = new }
    public var Spinner<T, M>.layout  : Layout?              get() = this._layout; set(new) { _layout = new }

    /**
     * Called whenever the Spinner's selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [Spinner.changed].
     *
     * @param spinner with change
     */
    public abstract fun changed(spinner: Spinner<T, M>)
}

@Suppress("PropertyName")
public open class Spinner<T, M: Model<T>>(public val model: M, public val itemVisualizer: ItemVisualizer<T, Any>? = null): View() {

    public fun next    (): Unit = model.next    ()
    public fun previous(): Unit = model.previous()

    public open val value      : T       get() = model.value
    public      val hasPrevious: Boolean get() = model.hasPrevious
    public      val hasNext    : Boolean get() = model.hasNext

    public var behavior: SpinnerBehavior<T, M>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    // Expose container APIs for behavior
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    public val changed: ChangeObservers<Spinner<T, M>> = changed_

    private val modelChanged: (Model<T>) -> Unit = {
        changed_()
    }

    init {
        this.model.changed += modelChanged
    }

    public companion object {
        public operator fun     invoke(progression: IntProgression, itemVisualizer: ItemVisualizer<Int, Any>? = null): Spinner<Int, IntModel>            = Spinner(IntModel (progression), itemVisualizer)
        public operator fun <T> invoke(values: List<T>,             itemVisualizer: ItemVisualizer<T,   Any>? = null): Spinner<T, ListModel<T, List<T>>> = Spinner(ListModel(values     ), itemVisualizer)
    }
}

public class MutableSpinner<T>(model: MutableModel<T>): Spinner<T, MutableModel<T>>(model) {
    override var value: T
        get(   ) = super.value
        set(new) { model.value = new }


//     private val mEditorGenerator = new DefaultEditorGenerator()

//    public void setEditorGenerator( final EditorGenerator aEditorGenerator )
//    {
//        setProperty( new AbstractNamedProperty<EditorGenerator>( EDITOR_GENERATOR )
//                     {
//                         @Override public EditorGenerator getValue()
//                         {
//                             return mEditorGenerator
//                         }
//
//                         @Override public void setValue( EditorGenerator aValue )
//                         {
//                             if( aValue == null ) { mEditorGenerator = new DefaultEditorGenerator(); }
//                             else                 { mEditorGenerator = aValue;                       }
//                         }
//                     },
//                     aEditorGenerator )
//    }
//
//    public EditorGenerator getEditorGenerator() { return mEditorGenerator; }
//
//    public interface EditorGenerator extends ItemEditor
//    {
//        View getView( Spinner aSpinner, Object aObject )
//    }
//
//    private static class DefaultEditorGenerator implements EditorGenerator
//    {
//        public DefaultEditorGenerator()
//        {
//            mLabel = new Label()
//
//            mLabel.setTextHorizontalAlignment( Location.RIGHT )
//        }
//
//        @Override public View getView( Spinner aSpinner, Object aObject )
//        {
//            mLabel.setText( aObject.toString() )
//
//            return mLabel
//        }
//
//        @Override public Object  getValue        (                    ) { return mLabel.getText(); }
//        @Override public Boolean isEditable      ( Event aEvent       ) { return false;            }
//        @Override public Boolean stopEditing     (                    ) { return false;            }
//        @Override public Boolean cancelEditing   (                    ) { return false;            }
//        @Override public Boolean shouldSelectItem( Event aEvent       ) { return false;            }
//        @Override public void    addListener     ( Listener aListener ) {                          }
//        @Override public void    removeListener  ( Listener aListener ) {                          }
//
//
//        private Label mLabel
//    }

    public companion object {
        public operator fun <T> invoke(values: MutableList<T> = mutableListOf()): MutableSpinner<T> = MutableSpinner(MutableListModel(values))
    }
}