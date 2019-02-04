package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Button.Button2
import com.nectar.doodle.system.SystemMouseEvent.Button.Button3
import com.nectar.doodle.system.SystemMouseEvent.Type
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent
import com.nectar.doodle.system.impl.MouseInputServiceStrategy.EventHandler
import com.nectar.doodle.utils.ifFalse
import com.nectar.doodle.utils.ifTrue
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent

internal class MouseInputServiceStrategyWebkit(private val htmlFactory: HtmlFactory): MouseInputServiceStrategy {

    override var toolTipText: String = ""
        set(new) {
            field              = new
            inputDevice?.title = new
        }

    override var cursor: Cursor = Cursor.Default
        set(new) {
            if (new != field) {
                inputDevice?.style?.cursor = new.toString()

                field = new
            }
    }

    override var mouseLocation = Origin
        private set

    private var inputDevice  = null as HTMLElement?
    private var eventHandler = null as EventHandler?

    override fun startUp(handler: EventHandler) {
        eventHandler = handler

        if (inputDevice == null) {
            inputDevice = htmlFactory.root.also {
                registerCallbacks(it)
            }
        }
    }

    override fun shutdown() {
        inputDevice?.let {
            unregisterCallbacks(it)

            inputDevice = null
        }
    }

    private fun mouseEnter(event: MouseEvent) {
        eventHandler?.handle(createMouseEvent(event, Enter, 0))
    }

    private fun mouseExit(event: MouseEvent) {
        eventHandler?.handle(createMouseEvent(event, Exit, 0))
    }

    private fun mouseUp(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Up, 1))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseDown(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Down, 1))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    // TODO: Remove this and just rely on vanilla down/up events since you usually get a single up right before a double click up
    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler?.handle(createMouseEvent(event, Up, 2))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseMove(event: MouseEvent): Boolean {
        mouseLocation = Point(x = event.clientX - htmlFactory.root.offsetLeft + htmlFactory.root.scrollLeft,
                y = event.clientY - htmlFactory.root.offsetTop + htmlFactory.root.scrollLeft)

        eventHandler?.handle(createMouseEvent(event, Move, 0))

        return isNativeElement(event.target).ifFalse {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun mouseScroll(event: WheelEvent): Boolean {
        val deltaX = 0 - event.deltaX / 28
        val deltaY = 0 - event.deltaY / 28

        val wheelEvent = SystemMouseWheelEvent(
                mouseLocation,
                deltaX.toInt(),
                deltaY.toInt(),
                createModifiers(event))

        eventHandler?.handle(wheelEvent)

        return !wheelEvent.consumed.also {
//            event.preventDefault ()
//            event.stopPropagation()
        }
    }

    private fun createMouseEvent(event: MouseEvent, aType: Type, clickCount: Int): SystemMouseEvent {
        val buttons    = mutableSetOf<SystemMouseEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        if (buttonsInt and 1 == 1) buttons.add(Button1)
        if (buttonsInt and 2 == 2) buttons.add(Button2)
        if (buttonsInt and 4 == 4) buttons.add(Button3)

        return SystemMouseEvent(
                aType,
                Point(mouseLocation.x, mouseLocation.y),
                buttons,
                clickCount,
                createModifiers(event),
                nativeScrollPanel(event.target))
    }

    private fun createModifiers(event: MouseEvent) = mutableSetOf<Modifier>().apply {
        event.altKey.ifTrue   { add(Alt  ) }
        event.ctrlKey.ifTrue  { add(Ctrl ) }
        event.metaKey.ifTrue  { add(Meta ) }
        event.shiftKey.ifTrue { add(Shift) }
    }

    private fun registerCallbacks(element: HTMLElement) = element.apply {
        onwheel     = { this@MouseInputServiceStrategyWebkit.mouseScroll(it) }
        onmouseup   = { this@MouseInputServiceStrategyWebkit.mouseUp    (it) }
        onmouseout  = { this@MouseInputServiceStrategyWebkit.mouseExit  (it) }
        ondblclick  = { this@MouseInputServiceStrategyWebkit.doubleClick(it) }
        onmousedown = { this@MouseInputServiceStrategyWebkit.mouseDown  (it) }
        onmousemove = { this@MouseInputServiceStrategyWebkit.mouseMove  (it) }
        onmouseover = { this@MouseInputServiceStrategyWebkit.mouseEnter (it) }
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.apply {
        onwheel     = null
        onmouseup   = null
        onmouseout  = null
        ondblclick  = null
        onmousedown = null
        onmousemove = null
        onmouseover = null
    }
}
