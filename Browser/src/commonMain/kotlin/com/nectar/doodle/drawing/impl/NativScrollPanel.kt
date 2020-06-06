package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.dom.EventTarget
import com.nectar.doodle.dom.Overflow.Hidden
import com.nectar.doodle.dom.Overflow.Scroll
import com.nectar.doodle.dom.scrollTo
import com.nectar.doodle.dom.setOverflow
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.willChange

/**
 * Created by Nicholas Eddy on 2/5/18.
 */

internal interface NativeScrollPanelFactory {
    operator fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit): NativeScrollPanel
}

internal class NativeScrollPanelFactoryImpl internal constructor(
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val handlerFactory: NativeEventHandlerFactory): NativeScrollPanelFactory {
    override fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit) = NativeScrollPanel(handlerFactory, graphicsDevice, scrollPanel, onScroll)
}

internal class NativeScrollPanel internal constructor(
                    handlerFactory: NativeEventHandlerFactory,
                    graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val panel         : ScrollPanel,
        private val scrolled      : (Point) -> Unit): NativeEventListener {

    private val eventHandler: NativeEventHandler
    private val rootElement = graphicsDevice[panel].rootElement

    init {
        rootElement.apply {
            style.setOverflow(Scroll())
            style.willChange = "transform"

            scrollTo(panel.scroll)
        }

        eventHandler = handlerFactory(rootElement, this).apply {
            registerScrollListener()
        }
    }

    fun discard() {
        rootElement.apply {
            style.setOverflow(Hidden())

            eventHandler.unregisterScrollListener()
        }
    }

    override fun onScroll(target: EventTarget?) = true.also {
        scrolled(Point(x = rootElement.scrollLeft, y = rootElement.scrollTop))
    }

    fun scrollTo(point: Point) {
        rootElement.scrollTo(point)
    }
}