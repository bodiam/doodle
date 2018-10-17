package com.nectar.doodle.drawing.impl

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsSurface
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/26/18.
 */
class RealGraphicsDeviceTests {
    @Test @JsName("create")
    fun `create works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface1       = mockk<GraphicsSurface>(relaxed = true)
        val surface2       = mockk<GraphicsSurface>(relaxed = true)

        every { surfaceFactory.surface() } returns surface1 andThen surface2

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface1) { device.create() }
        expect(surface2) { device.create() }

        verify(exactly = 1) { surface1.zIndex = 0 }
        verify(exactly = 1) { surface2.zIndex = 0 }
    }

    @Test @JsName("getNoParentNoChildrenWorks")
    fun `get no parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface        = mockk<GraphicsSurface>(relaxed = true)

        every { surfaceFactory.surface(null, false) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[object: Gizmo() {}] }

        verify(exactly = 1) { surface.zIndex = 0 }
    }

    @Test @JsName("getNoParentWithChildrenWorks")
    fun `get no parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface        = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}

        parent.children_ += child

        every { surfaceFactory.surface(null, true) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[parent] }

        verify(exactly = 1) { surface.zIndex = 0 }
    }

    @Test @JsName("getWithParentNoChildrenWorks")
    fun `get with parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}

        parent.children_ += child

        every { surfaceFactory.surface(null,          true ) } returns parentSurface
        every { surfaceFactory.surface(parentSurface, false) } returns childSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent] }
        expect(childSurface ) { device[child ] }

        verify(exactly = 1) { parentSurface.zIndex = 0 }
        verify(exactly = 1) { childSurface.zIndex  = 0 }
    }

    @Test @JsName("getWithParentWithChildrenWorks")
    fun `get with parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface  = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}
        val nested         = object: Gizmo() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory.surface(null,          true ) } returns parentSurface
        every { surfaceFactory.surface(parentSurface, true ) } returns childSurface
        every { surfaceFactory.surface(childSurface,  false) } returns nestedSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent  ] }
        expect(childSurface ) { device[child   ] }
        expect(nestedSurface ) { device[nested ] }

        verify(exactly = 1) { parentSurface.zIndex  = 0 }
        verify(exactly = 1) { childSurface.zIndex   = 0 }
        verify(exactly = 1) { nestedSurface.zIndex  = 0 }
    }

    @Test @JsName("resultsCached")
    fun `results cached`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface  = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}
        val nested         = object: Gizmo() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory.surface(null,          true ) } returns parentSurface andThenThrows Exception()
        every { surfaceFactory.surface(parentSurface, true ) } returns childSurface  andThenThrows Exception()
        every { surfaceFactory.surface(childSurface,  false) } returns nestedSurface andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        (0..2).forEach {
            expect(parentSurface) { device[parent] }
            expect(childSurface) { device[child] }
            expect(nestedSurface) { device[nested] }
        }

        verify(exactly = 1) { parentSurface.zIndex  = 0 }
        verify(exactly = 1) { childSurface.zIndex   = 0 }
        verify(exactly = 1) { nestedSurface.zIndex  = 0 }
    }

    @Test @JsName("releaseGizmo")
    fun `release gizmo`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface1  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val parentSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface2  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}
        val nested         = object: Gizmo() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory.surface(null,           true ) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory.surface(parentSurface1, true ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory.surface(childSurface1,  false) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory.surface(parentSurface2, true ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory.surface(childSurface2,  false) } returns nestedSurface2 andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        (0..2).forEach {
            expect(parentSurface1) { device[parent] }
            expect(childSurface1 ) { device[child ] }
            expect(nestedSurface1) { device[nested] }
        }

        device.release(parent)

        (0..2).forEach {
            expect(parentSurface2) { device[parent] }
            expect(childSurface2 ) { device[child ] }
            expect(nestedSurface2) { device[nested] }
        }
    }


    @Test @JsName("releaseSurface")
    fun `release surface`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface1  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val parentSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface2  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: Gizmo() {}
        val nested         = object: Gizmo() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory.surface(null,           true ) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory.surface(parentSurface1, true ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory.surface(childSurface1,  false) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory.surface(parentSurface2, true ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory.surface(childSurface2,  false) } returns nestedSurface2 andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        (0..2).forEach {
            expect(parentSurface1) { device[parent] }
            expect(childSurface1 ) { device[child ] }
            expect(nestedSurface1) { device[nested] }
        }

        device.release(parentSurface1)

        (0..2).forEach {
            expect(parentSurface2) { device[parent] }
            expect(childSurface2 ) { device[child ] }
            expect(nestedSurface2) { device[nested] }
        }
    }
}