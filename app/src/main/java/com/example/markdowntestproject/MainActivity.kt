package com.example.markdowntestproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.markdowntestproject.ui.theme.MarkdownTestProjectTheme
import org.commonmark.node.Document
import org.commonmark.parser.InlineParserFactory
import org.commonmark.parser.Parser

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parser = Parser.builder().inlineParserFactory(getInlineParserFactory()).build()
        val root = parser.parse("sdff **sdfdf** zzffffg  uc-project://sdfsdfdfdf ==dfgdfg==  https://github.com  @eliseev dflkgeglk @zakharov") as Document
        setContent {
            var render by remember { mutableStateOf(true) }
            MarkdownTestProjectTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                render = !render
                            }
                        )
                    }) {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                MDDocument(root)
                        }
                    }
                }
            }
        }
    }


    fun getInlineParserFactory(): InlineParserFactory? {
        return InlineParserFactory { inlineParserContext -> InlineParserCustom(inlineParserContext) }
    }
}
