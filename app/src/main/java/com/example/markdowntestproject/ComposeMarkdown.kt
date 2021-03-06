package com.example.markdowntestproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.example.markdowntestproject.customnode.HttpNode
import com.example.markdowntestproject.customnode.MentionNode
import com.example.markdowntestproject.customnode.ProjectNode
import org.commonmark.node.*
import org.commonmark.node.Paragraph


/**
 * These functions will render a tree of Markdown nodes parsed with CommonMark.
 * Images will be rendered using Chris Banes Accompanist library (which uses Coil)
 *
 * To use this, you need the following two dependencies:
 * implementation "com.atlassian.commonmark:commonmark:0.15.2"
 * implementation "dev.chrisbanes.accompanist:accompanist-coil:0.2.0"
 *
 * The following is an example of how to use this:
 * ```
 * val parser = Parser.builder().build()
 * val root = parser.parse(MIXED_MD) as Document
 * val markdownComposer = MarkdownComposer()
 *
 * MarkdownComposerTheme {
 *    MDDocument(root)
 * }
 * ```
 */
private const val TAG_URL = "url"
private const val TAG_IMAGE_URL = "imageUrl"
private const val TAG_PROJECT = "project"
private const val TAG_MENTION = "mention"

@Composable
fun MDDocument(document: Document) {
    MDBlockChildren(document)
}

@Composable
fun MDHeading(heading: Heading, modifier: Modifier = Modifier) {
    val style = when (heading.level) {
        1 -> MaterialTheme.typography.h1
        2 -> MaterialTheme.typography.h2
        3 -> MaterialTheme.typography.h3
        4 -> MaterialTheme.typography.h4
        5 -> MaterialTheme.typography.h5
        6 -> MaterialTheme.typography.h6
        else -> {
            // Invalid header...
            MDBlockChildren(heading)
            return
        }
    }

    val padding = if (heading.parent is Document) 8.dp else 0.dp
    Box(modifier = modifier.padding(bottom = padding)) {
        val text = buildAnnotatedString {
            appendMarkdownChildren(heading, MaterialTheme.colors)
        }
        MarkdownText(text, style)
    }
}

@Composable
fun MDParagraph(paragraph: Paragraph, modifier: Modifier = Modifier) {
    if (paragraph.firstChild is Image && paragraph.firstChild == paragraph.lastChild) {
        // Paragraph with single image
        MDImage(paragraph.firstChild as Image, modifier)
    } else {
        val padding = if (paragraph.parent is Document) 8.dp else 0.dp
        Box(modifier = modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MaterialTheme.typography.body1.toSpanStyle())
                appendMarkdownChildren(paragraph, MaterialTheme.colors)
                pop()
            }
            MarkdownText(styledText, MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun MDImage(image: Image, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = rememberImagePainter(
                data = image.destination,
                builder = {
                    size(OriginalSize)
                },
            ),
            contentDescription = null,
        )
    }
}

@Composable
fun MDBulletList(bulletList: BulletList, modifier: Modifier = Modifier) {
    val marker = bulletList.bulletMarker
    MDListItems(bulletList, modifier = modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("$marker ")
            appendMarkdownChildren(it, MaterialTheme.colors)
            pop()
        }
        MarkdownText(text, MaterialTheme.typography.body1, modifier)
    }
}

@Composable
fun MDOrderedList(orderedList: OrderedList, modifier: Modifier = Modifier) {
    var number = orderedList.startNumber
    val delimiter = orderedList.delimiter
    MDListItems(orderedList, modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("${number++}$delimiter ")
            appendMarkdownChildren(it, MaterialTheme.colors)
            pop()
        }
        MarkdownText(text, MaterialTheme.typography.body1, modifier)
    }
}

@Composable
fun MDListItems(
    listBlock: ListBlock,
    modifier: Modifier = Modifier,
    item: @Composable (node: Node) -> Unit
) {
    val bottom = if (listBlock.parent is Document) 8.dp else 0.dp
    val start = if (listBlock.parent is Document) 0.dp else 8.dp
    Column(modifier = modifier.padding(start = start, bottom = bottom)) {
        var listItem = listBlock.firstChild
        while (listItem != null) {
            var child = listItem.firstChild
            while (child != null) {
                when (child) {
                    is BulletList -> MDBulletList(child, modifier)
                    is OrderedList -> MDOrderedList(child, modifier)
                    else -> item(child)
                }
                child = child.next
            }
            listItem = listItem.next
        }
    }
}

@Composable
fun MDBlockQuote(blockQuote: BlockQuote, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colors.onBackground
    Box(modifier = modifier
        .drawBehind {
            drawLine(
                color = color,
                strokeWidth = 2f,
                start = Offset(12.dp.value, 0f),
                end = Offset(12.dp.value, size.height)
            )
        }
        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        val text = buildAnnotatedString {
            pushStyle(
                MaterialTheme.typography.body1.toSpanStyle()
                    .plus(SpanStyle(fontStyle = FontStyle.Italic))
            )
            appendMarkdownChildren(blockQuote, MaterialTheme.colors)
            pop()
        }
        Text(text, modifier)
    }
}

@Composable
fun MDFencedCodeBlock(fencedCodeBlock: FencedCodeBlock, modifier: Modifier = Modifier) {
    val padding = if (fencedCodeBlock.parent is Document) 8.dp else 0.dp
    Box(modifier = modifier.padding(start = 8.dp, bottom = padding)) {
        Text(
            text = fencedCodeBlock.literal,
            style = TextStyle(fontFamily = FontFamily.Monospace),
            modifier = modifier
        )
    }
}

@Composable
fun MDIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock, modifier: Modifier = Modifier) {
    // Ignored
}

@Composable
fun MDThematicBreak(thematicBreak: ThematicBreak, modifier: Modifier = Modifier) {
    //Ignored
}

@Composable
fun MDBlockChildren(parent: Node) {
    var child = parent.firstChild
    while (child != null) {
        Log.d("Markdown", "MDBlockChildren child = $child")
        when (child) {
            is BlockQuote -> MDBlockQuote(child)
            is ThematicBreak -> MDThematicBreak(child)
            is Heading -> MDHeading(child)
            is Paragraph -> MDParagraph(child)
            is FencedCodeBlock -> MDFencedCodeBlock(child)
            is IndentedCodeBlock -> MDIndentedCodeBlock(child)
            is Image -> MDImage(child)
            is BulletList -> MDBulletList(child)
            is OrderedList -> MDOrderedList(child)
        }
        child = child.next
    }
}

fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node, colors: Colors
) {
    var child = parent.firstChild
    while (child != null) {
        Log.d("Markdown", "child = $child")
        when (child) {
            is MentionNode -> {
                val mentionStyle = SpanStyle(Color.Blue)
                pushStyle(mentionStyle)
                pushStringAnnotation(TAG_MENTION, child.mention)
                append(child.mention)
                pop()
                pop()
            }
            is HttpNode -> {
                val underline = SpanStyle(colors.primary, textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                pushStringAnnotation(TAG_URL, child.url)
                append(child.url)
                pop()
                pop()
            }
            is ProjectNode -> {
                val style = SpanStyle(Color.White, background = Color.Blue, baselineShift = BaselineShift(0.1f))
                pushStyle(style)
                pushStringAnnotation(TAG_PROJECT, child.destination)
                val unicode = 0x25B6
                append("  ?????????????? ???????????? ${String(Character.toChars(unicode))}")
                pop()
                pop()
            }
            is Paragraph -> appendMarkdownChildren(child, colors)
            is Text -> append(child.literal)
            is Image -> appendInlineContent(TAG_IMAGE_URL, child.destination)
            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is Code -> {
                pushStyle(TextStyle(fontFamily = FontFamily.Monospace).toSpanStyle())
                append(child.literal)
                pop()
            }
            is HardLineBreak -> {
                append("\n")
            }
            is Link -> {
                val underline = SpanStyle(colors.primary, textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                pushStringAnnotation(TAG_URL, child.destination)
                appendMarkdownChildren(child, colors)
                pop()
                pop()
            }
        }
        child = child.next
    }
}

@Composable
fun MarkdownText(text: AnnotatedString, style: TextStyle, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val context = LocalContext.current
    Text(text = text,
        modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(offset)
                    text.getStringAnnotations(position, position)
                        .firstOrNull()
                        ?.let { sa ->
                            when(sa.tag){
                                TAG_URL -> uriHandler.openUri(sa.item)
                                TAG_PROJECT -> Toast.makeText(context, sa.item, Toast.LENGTH_SHORT).show()
                                TAG_MENTION -> Toast.makeText(context, sa.item, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        },
        style = style,
        inlineContent = mapOf(
            TAG_IMAGE_URL to InlineTextContent(
                Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Bottom)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = it,
                    ),
                    contentDescription = null,
                    modifier = modifier,
                    alignment = Alignment.Center
                )

            }
        ),
        onTextLayout = { layoutResult.value = it }
    )
}