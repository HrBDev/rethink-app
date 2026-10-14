/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.fields

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Looper
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.celzero.bravedns.R
import com.celzero.bravedns.ui.compose.RethinkTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Independent real AppCompat widget oracle with plain Application and synthetic text only.
 * Authored, not run: source checks are not Android compile, runtime, or pixel-equivalence evidence.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
class OriginalSearchStyleReferenceTest {
    @get:Rule val compose = createComposeRule()
    private val themes = listOf(R.style.AppThemeWhite, R.style.AppTheme, R.style.AppThemeTrueBlack,
        R.style.AppThemeWhitePlus, R.style.AppThemeTrueBlackPlus, R.style.AppThemeTrueBlackFrost)

    @Test fun searchAutoCompleteColorsPaddingAndPlatePixelsMatchReferenceAcrossThemesAndFocus() {
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), theme)
            val native = SearchView(context).apply { setIconifiedByDefault(false) }
            val editor = native.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
            val plate = native.findViewById<View>(androidx.appcompat.R.id.search_plate)
            val resources = resolveFieldResources(context, OriginalFieldStyle.Search)
            val search = checkNotNull(resources.search)
            native.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            assertEquals(native.measuredHeight.toFloat(), resources.minHeight * context.resources.displayMetrics.density, .01f)
            assertEquals(Rect(editor.paddingLeft, editor.paddingTop, editor.paddingRight, editor.paddingBottom), search.editorPadding)
            assertEquals(editor.includeFontPadding, search.includeFontPadding)
            assertEquals(editor.highlightColor, resources.highlight.toArgb())
            val nativePadding = Rect().also { plate.background.getPadding(it) }
            assertEquals(nativePadding, resources.padding)
            assertNotNull(resources.background)
            for (focused in listOf(false, true, false)) {
                if (focused) assertTrue(editor.requestFocus()) else native.clearFocus()
                shadowOf(Looper.getMainLooper()).idle()
                // SearchView itself sets focused or empty plate state; the resolver does not
                // synthesize a disabled state which would change AppCompat's tint selector.
                assertArrayEquals(plate.background.state, searchPlateStateSet(focused))
                resources.background!!.state = searchPlateStateSet(focused)
                assertTrue(pixels(plate.background).contentEquals(pixels(resources.background)))
                for (enabled in listOf(true, false)) {
                    editor.isEnabled = enabled
                    assertEquals(editor.currentTextColor, resources.text!!.fieldColor(enabled, focused).toArgb())
                    assertEquals(editor.currentHintTextColor, resources.hint!!.fieldColor(enabled, focused).toArgb())
                }
                editor.isEnabled = true
            }
            val magnifier = native.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
            assertEquals(magnifier.layoutParams.width.toFloat(), search.iconSlotWidth * context.resources.displayMetrics.density, .01f)
        }
    }

    @Test fun serverOverridesOnlyEditorDensityAndKeepsNativeQueryBackground() {
        themes.forEach { theme ->
            val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), theme)
            val native = SearchView(context).apply { setIconifiedByDefault(false) }
            val editor = native.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text).apply {
                textSize = 14f; includeFontPadding = false; setPadding(0, 0, 0, 0)
            }
            val resources = resolveFieldResources(context, OriginalFieldStyle.ServerSearch)
            assertEquals(40f, resources.minHeight, 0f)
            assertEquals(Rect(editor.paddingLeft, editor.paddingTop, editor.paddingRight, editor.paddingBottom), resources.search!!.editorPadding)
            assertFalse(resources.search.includeFontPadding)
            assertNotNull(resources.background)
            assertNotNull(resources.search.icon)
            assertEquals(32f, resources.search.clearWidth, .01f)
            assertEquals(editor.textColors.defaultColor, resources.text!!.defaultColor)
            // The app-log layouts explicitly remove both search icons, but keep the plate.
            val appLog = resolveFieldResources(context, OriginalFieldStyle.AppLogSearch)
            assertNull(appLog.search!!.icon)
            assertNotNull(appLog.background)
        }
    }

    @Test fun typographyComesFromNativeSearchAutoCompleteRatherThanPlainEditText() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
        val native = SearchView(context).apply { setIconifiedByDefault(false) }
        val editor = native.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val pixels = mutableMapOf<OriginalFieldStyle, Float>()
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                val density = LocalDensity.current
                listOf(OriginalFieldStyle.Search, OriginalFieldStyle.AppLogSearch, OriginalFieldStyle.ServerSearch).forEach { style ->
                    val text: TextStyle = originalSearchTextStyle(style)
                    SideEffect { pixels[style] = with(density) { text.fontSize.toPx() } }
                }
            }
        }
        compose.runOnIdle {
            assertEquals(editor.textSize, pixels.getValue(OriginalFieldStyle.Search), .01f)
            editor.textSize = 14f
            assertEquals(editor.textSize, pixels.getValue(OriginalFieldStyle.AppLogSearch), .01f)
            assertEquals(editor.textSize, pixels.getValue(OriginalFieldStyle.ServerSearch), .01f)
        }
    }

    @Test fun serverFortyDpBoundsSurviveTypingClearingAndFocusWithoutExtraCallbacks() {
        var value by mutableStateOf("")
        var changes = 0
        var clears = 0
        compose.setContent {
            val base = LocalContext.current
            val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme {
                    Column {
                        OriginalTextField(value, { value = it; changes++ }, Modifier.width(280.dp).height(40.dp).testTag("server"),
                            style = OriginalFieldStyle.ServerSearch, singleLine = true,
                            placeholder = { Text("Synthetic search") }, trailingIcon = {
                                if (value.isNotEmpty()) IconButton({ value = ""; clears++ }, Modifier.testTag("clear")) {
                                    Icon(painterResource(R.drawable.ic_cross), "Clear synthetic search")
                                }
                            })
                    }
                }
            }
        }
        compose.onNodeWithTag("server").assertHeightIsEqualTo(40.dp).performClick()
        compose.runOnIdle { assertEquals(0, changes) }
        compose.onNodeWithTag("server").performTextReplacement("synthetic")
        compose.onNodeWithTag("server").assertHeightIsEqualTo(40.dp)
        compose.onNodeWithTag("clear").performClick()
        compose.onNodeWithTag("server").assertHeightIsEqualTo(40.dp)
        compose.runOnIdle { assertEquals(1, changes); assertEquals(1, clears); assertEquals("", value) }
        compose.onNodeWithTag("clear").assertDoesNotExist()
    }

    @Test fun defaultSearchKeyboardOptionsMatchNativeActionAndDisableAutoCorrection() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppThemeWhite)
        val native = SearchView(context).apply { setIconifiedByDefault(false) }
        val editor = native.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        // Independent dependency oracle: do not derive these expectations from our helper.
        // AppCompat abc_search_view.xml blob 33d25d9ec40b10fc7a1b6cf5b615252dba1b802c:
        // android:imeOptions="actionSearch", inputType="text|textAutoComplete|textNoSuggestions".
        assertEquals(EditorInfo.IME_ACTION_SEARCH, editor.imeOptions and EditorInfo.IME_MASK_ACTION)
        assertTrue(editor.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0)
        assertTrue(editor.inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE != 0)
        assertEquals(0, editor.inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        listOf(OriginalFieldStyle.Search, OriginalFieldStyle.AppLogSearch, OriginalFieldStyle.ServerSearch).forEach { style ->
            val options = originalFieldKeyboardOptions(style, KeyboardOptions.Default)
            assertEquals(ImeAction.Search, options.imeAction)
            assertEquals(false, options.autoCorrectEnabled)
        }
        // No claim of bit-for-bit EditorInfo parity: Compose's public option disables
        // autocorrection but does not add the native autocomplete/no-suggestions flag pair.
    }

    @Test fun explicitSearchOptionsAndEveryNonSearchFamilyAreUnchanged() {
        val locales = LocaleList("fa,en")
        for (action in listOf(ImeAction.Default, ImeAction.None, ImeAction.Done, ImeAction.Next)) {
            for (autoCorrect in listOf(false, true)) {
                val explicit = KeyboardOptions(capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = autoCorrect, keyboardType = KeyboardType.Uri,
                    imeAction = action, showKeyboardOnFocus = false, hintLocales = locales)
                OriginalFieldStyle.entries.forEach { style ->
                    val actual = originalFieldKeyboardOptions(style, explicit)
                    assertEquals(explicit, actual)
                    if (!style.search) assertSame(explicit, actual)
                }
            }
        }
        OriginalFieldStyle.entries.filterNot { it.search }.forEach { style ->
            assertSame(KeyboardOptions.Default, originalFieldKeyboardOptions(style, KeyboardOptions.Default))
        }
        val unsetAction = KeyboardOptions(autoCorrectEnabled = true, keyboardType = KeyboardType.Email,
            showKeyboardOnFocus = false, hintLocales = locales)
        val filled = originalFieldKeyboardOptions(OriginalFieldStyle.ServerSearch, unsetAction)
        assertEquals(ImeAction.Search, filled.imeAction)
        assertEquals(true, filled.autoCorrectEnabled)
        assertEquals(KeyboardType.Email, filled.keyboardType)
        assertEquals(false, filled.showKeyboardOnFocus)
        assertEquals(locales, filled.hintLocales)
    }

    @Test fun bothOverloadsExposeSearchImeAndKeepCallerSubmissionCallbacks() {
        val submitted = mutableListOf<String>()
        var changes = 0
        compose.setContent {
            val base = LocalContext.current
            val context = remember(base) { ContextThemeWrapper(base, R.style.AppThemeWhite) }
            CompositionLocalProvider(LocalContext provides context) {
                RethinkTheme {
                    Column {
                        listOf(OriginalFieldStyle.Search, OriginalFieldStyle.AppLogSearch, OriginalFieldStyle.ServerSearch).forEach { style ->
                            OriginalTextField("synthetic", { changes++ }, Modifier.width(280.dp).testTag(style.name),
                                style = style, singleLine = true,
                                keyboardActions = KeyboardActions(onSearch = { submitted += style.name }))
                        }
                        OriginalTextField(TextFieldValue("synthetic"), { changes++ }, Modifier.width(280.dp).testTag("value-overload"),
                            style = OriginalFieldStyle.ServerSearch, singleLine = true,
                            keyboardActions = KeyboardActions(onSearch = { submitted += "value-overload" }))
                        OriginalTextField("synthetic", { changes++ }, Modifier.width(280.dp).testTag("explicit-done"),
                            style = OriginalFieldStyle.Search, singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = true),
                            keyboardActions = KeyboardActions(onDone = { submitted += "explicit-done" }))
                    }
                }
            }
        }
        val searchTags = listOf("Search", "AppLogSearch", "ServerSearch", "value-overload")
        searchTags.forEach { tag ->
            compose.onNodeWithTag(tag).assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Search))
                .performImeAction()
        }
        compose.onNodeWithTag("explicit-done").assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Done))
            .performImeAction()
        compose.runOnIdle { assertEquals(searchTags + "explicit-done", submitted); assertEquals(0, changes) }
    }

    private fun pixels(drawable: Drawable): IntArray {
        val bitmap = Bitmap.createBitmap(240, 48, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(Canvas(bitmap))
        return IntArray(bitmap.width * bitmap.height).also { bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height); bitmap.recycle() }
    }
}
