package com.maubis.markdown.segmenter

class MarkdownSegmentBuilder {
  var config: ISegmentConfig = InvalidSegment(MarkdownSegmentType.INVALID)
  val builder: StringBuilder = StringBuilder()

  fun build(): MarkdownSegment {
    val text = builder.toString()
    val segmentConfig = config
    return when (segmentConfig) {
      is LineStartSegment -> LineStartMarkdownSegment(segmentConfig, text)
      is LineDelimiterSegment -> LineDelimiterMarkdownSegment(segmentConfig, text)
      is MultilineDelimiterSegment -> MultilineDelimiterMarkdownSegment(segmentConfig, text)
      is MultilineStartSegment -> MultilineStartMarkdownSegment(segmentConfig, text)
      else -> NormalMarkdownSegment(segmentConfig, text)
    }
  }
}

abstract class MarkdownSegment {

  abstract fun type(): MarkdownSegmentType

  abstract fun strip(): String

  abstract fun text(): String
}

class NormalMarkdownSegment(val config: ISegmentConfig, val text: String) : MarkdownSegment() {
  override fun type() = config.type()

  override fun strip(): String {
    return text
  }

  override fun text(): String = text
}

class LineStartMarkdownSegment(val config: LineStartSegment, val text: String) : MarkdownSegment() {
  override fun type() = config.type()

  override fun strip(): String {
    return "${config.replacementToken}${text.removePrefix(config.lineStartToken)}"
  }

  override fun text(): String = text
}

class LineDelimiterMarkdownSegment(val config: LineDelimiterSegment, val text: String) : MarkdownSegment() {
  override fun type() = config.type()

  override fun strip(): String {
    return text.removePrefix(config.lineStartToken).trim().removeSuffix(config.lineEndToken)
  }

  override fun text(): String = text
}

class MultilineDelimiterMarkdownSegment(val config: MultilineDelimiterSegment, val text: String) : MarkdownSegment() {
  override fun type() = config.type()

  override fun strip(): String {
    return text.trim()
        .removePrefix(config.multilineStartToken).trim()
        .removeSuffix(config.multilineEndToken).trim()
  }

  override fun text(): String = text
}

class MultilineStartMarkdownSegment(val config: MultilineStartSegment, val text: String) : MarkdownSegment() {
  override fun type() = config.type()

  override fun strip(): String {
    return text.trim()
        .removePrefix(config.multilineStartToken).trim()
  }

  override fun text(): String = text
}