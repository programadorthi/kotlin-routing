import dev.programadorthi.routing.core.handle
import dev.programadorthi.routing.core.replace
import dev.programadorthi.routing.core.routing
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toLong
import kotlinx.cinterop.wcstr
import platform.windows.BeginPaint
import platform.windows.CS_HREDRAW
import platform.windows.CS_VREDRAW
import platform.windows.CW_USEDEFAULT
import platform.windows.CreateSolidBrush
import platform.windows.CreateWindowEx
import platform.windows.DefWindowProc
import platform.windows.DeleteObject
import platform.windows.DispatchMessage
import platform.windows.EndPaint
import platform.windows.FillRect
import platform.windows.GetMessage
import platform.windows.GetModuleHandle
import platform.windows.HWND
import platform.windows.IDC_ARROW
import platform.windows.IDI_APPLICATION
import platform.windows.IDOK
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.LoadCursor
import platform.windows.LoadIcon
import platform.windows.MB_OKCANCEL
import platform.windows.MSG
import platform.windows.MessageBox
import platform.windows.NULL
import platform.windows.PAINTSTRUCT
import platform.windows.PostMessageW
import platform.windows.RegisterClassEx
import platform.windows.ShowWindow
import platform.windows.TextOutW
import platform.windows.TranslateMessage
import platform.windows.UINT
import platform.windows.UpdateWindow
import platform.windows.WHITE_BRUSH
import platform.windows.WM_CLOSE
import platform.windows.WM_DESTROY
import platform.windows.WM_PAINT
import platform.windows.WM_QUIT
import platform.windows.WNDCLASSEX
import platform.windows.WPARAM
import platform.windows.WS_EX_OVERLAPPEDWINDOW
import platform.windows.WS_OVERLAPPEDWINDOW

var globalHwnd: HWND? = null

val router = routing {
    handle(path = "/exit") {
        // Unfortunately, PostQuitMessage can not be called in another thread
        // PostQuitMessage(0)
        PostMessageW(
            globalHwnd,
            WM_QUIT.toUInt(),
            0u,
            0L
        )
    }

    handle(path = "/askBeforeExit") {
        memScoped {
            if (MessageBox!!(
                    globalHwnd,
                    "Really quit?".wcstr.ptr,
                    "My application".wcstr.ptr,
                    MB_OKCANCEL.toUInt()
                ) == IDOK
            ) {
                // Unfortunately, DestroyWindow can not be called in another thread
                // DestroyWindow(globalHwnd)
                PostMessageW(
                    globalHwnd,
                    WM_DESTROY.toUInt(),
                    0u,
                    0L
                )
            }
        }
    }

    handle(path = "/home") {
        memScoped {
            val ps = alloc<PAINTSTRUCT>()
            val hdc = BeginPaint(globalHwnd, ps.ptr)
            val brush = CreateSolidBrush(0x00FFFFFF)

            FillRect(hdc, ps.rcPaint.ptr, brush)

            val greeting = "Hello, Windows desktop!"
            TextOutW(
                hdc,
                5, 5,
                greeting,
                greeting.length
            )

            DeleteObject(brush)
            DeleteObject(hdc)

            EndPaint(globalHwnd, ps.ptr)
        }
    }
}

fun wndProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    globalHwnd = hwnd

    // This when block differentiates between the message type that could have been received. If you want to
    // handle a specific type of message in your application, just define it in this block.
    when (msg) {
        WM_CLOSE.toUInt() -> router.replace(path = "/askBeforeExit")

        WM_DESTROY.toUInt() -> router.replace(path = "/exit")

        WM_PAINT.toUInt() -> router.replace(path = "/home")

        else -> return (DefWindowProc!!)(hwnd, msg, wParam, lParam)
    }

    return 0
}

fun main() = memScoped {
    val szWindowClass = "DesktopApp".wcstr.ptr
    val szTitle = "Windows Desktop Guided Tour Application".wcstr.ptr

    val hInstance = (GetModuleHandle!!)(null)
    val wcex = alloc<WNDCLASSEX>()

    wcex.cbSize = sizeOf<WNDCLASSEX>().toUInt()
    wcex.style = (CS_HREDRAW or CS_VREDRAW).toUInt()
    wcex.lpfnWndProc = staticCFunction(::wndProc)
    wcex.cbClsExtra = 0
    wcex.cbWndExtra = 0
    wcex.hInstance = hInstance
    wcex.hIcon = LoadIcon!!(wcex.hInstance, IDI_APPLICATION)
    wcex.hCursor = LoadCursor!!(null, IDC_ARROW)
    wcex.hbrBackground = CreateSolidBrush(WHITE_BRUSH)
    wcex.lpszMenuName = null
    wcex.lpszClassName = szWindowClass
    wcex.hIconSm = LoadIcon!!(wcex.hInstance, IDI_APPLICATION)

    if (RegisterClassEx!!(wcex.ptr) < 1u) {
        MessageBox!!(
            null,
            "Call to RegisterClassEx failed!".wcstr.ptr,
            "Windows Desktop Guided Tour".wcstr.ptr,
            NULL.toLong().toUInt()
        )
        return@memScoped
    }

    // The parameters to CreateWindowEx explained:
    // WS_EX_OVERLAPPEDWINDOW : An optional extended window style.
    // szWindowClass: the name of the application
    // szTitle: the text that appears in the title bar
    // WS_OVERLAPPEDWINDOW: the type of window to create
    // CW_USEDEFAULT, CW_USEDEFAULT: initial position (x, y)
    // 500, 100: initial size (width, length)
    // NULL: the parent of this window
    // NULL: this application does not have a menu bar
    // hInstance: the first parameter from WinMain
    // NULL: not used in this application
    val hWnd = CreateWindowEx!!(
        WS_EX_OVERLAPPEDWINDOW.toUInt(),
        szWindowClass,
        szTitle,
        WS_OVERLAPPEDWINDOW.toUInt(),
        CW_USEDEFAULT, CW_USEDEFAULT,
        800, 600,
        null,
        null,
        hInstance,
        NULL
    )
    if (hWnd == null) {
        MessageBox!!(
            null,
            "Call to CreateWindowEx failed!".wcstr.ptr,
            "Windows Desktop Guided Tour".wcstr.ptr,
            NULL.toLong().toUInt()
        )
        return@memScoped
    }

    ShowWindow(hWnd, 1)
    UpdateWindow(hWnd)

    val msg = alloc<MSG>()
    while (GetMessage!!(msg.ptr, null, 0u, 0u) > 0) {
        TranslateMessage(msg.ptr)
        DispatchMessage!!(msg.ptr)
    }
}