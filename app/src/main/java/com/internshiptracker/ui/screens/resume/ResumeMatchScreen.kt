package com.internshiptracker.ui.screens.resume

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.viewmodel.AddEditViewModel
import kotlin.math.roundToInt

/**
 * Resume Match Score screen.
 *
 * Algorithm (keyword-based NLP):
 *   1. Tokenise both the job description and resume into lowercase words
 *   2. Remove common stop words
 *   3. Count how many JD keywords appear in the resume
 *   4. Score = matched / total_jd_keywords * 100 (clamped 0-100)
 *
 * This is a realistic "basic NLP" approach that works offline,
 * is explainable, and produces meaningful scores for demo purposes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeMatchScreen(
    applicationId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    LaunchedEffect(applicationId) {
        viewModel.loadApplication(applicationId)
    }

    val companyName by viewModel.companyName.collectAsState()
    val role by viewModel.role.collectAsState()

    var jobDescription by remember { mutableStateOf("") }
    var resumeText by remember { mutableStateOf("") }
    var matchResult by remember { mutableStateOf<MatchResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Match") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Context banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Work, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(companyName, fontWeight = FontWeight.SemiBold)
                        Text(role, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Score display (if analyzed)
            matchResult?.let { result ->
                ScoreDisplay(result = result)
            }

            // Input: Job Description
            Text(
                "Job Description",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = jobDescription,
                onValueChange = {
                    jobDescription = it
                    matchResult = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                placeholder = { Text("Paste the job description here…") },
                minLines = 5,
                maxLines = 10
            )

            // Input: Resume
            Text(
                "Your Resume",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = resumeText,
                onValueChange = {
                    resumeText = it
                    matchResult = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                placeholder = { Text("Paste your resume text here…") },
                minLines = 5,
                maxLines = 10
            )

            // Sample data button for demo
            OutlinedButton(
                onClick = {
                    jobDescription = SAMPLE_JOB_DESCRIPTION
                    resumeText = SAMPLE_RESUME
                    matchResult = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text("Load Sample Data")
            }

            // Analyze button
            Button(
                onClick = {
                    isAnalyzing = true
                    matchResult = analyzeMatch(jobDescription, resumeText)
                    isAnalyzing = false
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = jobDescription.length > 50 && resumeText.length > 50 && !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Analytics, null)
                }
                Spacer(Modifier.width(8.dp))
                Text("Analyze Match")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScoreDisplay(result: MatchResult) {
    val animScore by animateFloatAsState(
        targetValue = result.score.toFloat(),
        animationSpec = tween(1200),
        label = "score"
    )

    val scoreColor = when {
        result.score >= 75 -> Color(0xFF4CAF50)
        result.score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Match Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Circular score indicator
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animScore / 100f },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 10.dp,
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.15f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${animScore.roundToInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            // Score interpretation
            val label = when {
                result.score >= 75 -> "Excellent Match 🎉"
                result.score >= 50 -> "Good Match 👍"
                result.score >= 25 -> "Moderate Match 🤔"
                else -> "Low Match 😕"
            }
            Text(label, style = MaterialTheme.typography.titleSmall, color = scoreColor)

            HorizontalDivider()

            // Matched keywords
            if (result.matchedKeywords.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "✅ Matched Keywords (${result.matchedKeywords.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    KeywordFlow(keywords = result.matchedKeywords, color = Color(0xFF4CAF50))
                }
            }

            // Missing keywords
            if (result.missingKeywords.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "❌ Missing Keywords (${result.missingKeywords.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    KeywordFlow(keywords = result.missingKeywords.take(10), color = Color(0xFFF44336))
                    if (result.missingKeywords.size > 10) {
                        Text(
                            "+${result.missingKeywords.size - 10} more keywords",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Tips
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "💡 Tips to improve your score:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    result.missingKeywords.take(3).forEach { kw ->
                        Text(
                            "• Add \"$kw\" to your resume if you have this skill",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeywordFlow(keywords: List<String>, color: Color) {
    // Simple wrap-around chip layout
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        keywords.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { kw ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            kw,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Keyword-based NLP matching algorithm ────────────────────────────────────

data class MatchResult(
    val score: Int,
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>
)

/** Common English stop words to exclude from keyword matching. */
private val STOP_WORDS = setOf(
    "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
    "have", "has", "had", "do", "does", "did", "will", "would", "could",
    "should", "may", "might", "shall", "can", "need", "dare", "ought",
    "used", "and", "but", "or", "nor", "for", "yet", "so", "to", "of",
    "in", "on", "at", "by", "with", "from", "up", "about", "into",
    "through", "during", "before", "after", "above", "below", "between",
    "out", "off", "over", "under", "again", "further", "then", "once",
    "here", "there", "when", "where", "why", "how", "all", "both", "each",
    "few", "more", "most", "other", "some", "such", "no", "not", "only",
    "own", "same", "than", "too", "very", "just", "because", "as", "until",
    "while", "although", "if", "unless", "since", "also", "well", "our",
    "your", "we", "you", "i", "he", "she", "it", "they", "this", "that"
)

/**
 * Core matching function.
 * Tokenises both texts, removes stop words, then computes keyword overlap.
 */
fun analyzeMatch(jobDescription: String, resumeText: String): MatchResult {
    fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split(Regex("[^a-z0-9+#.]+"))
            .filter { it.length >= 3 && it !in STOP_WORDS }
            .toSet()

    val jdKeywords = tokenize(jobDescription)
    val resumeKeywords = tokenize(resumeText)

    val matched = jdKeywords.intersect(resumeKeywords).toList().sorted()
    val missing = (jdKeywords - resumeKeywords).toList().sorted()

    val score = if (jdKeywords.isEmpty()) 0
    else ((matched.size.toFloat() / jdKeywords.size) * 100).roundToInt().coerceIn(0, 100)

    return MatchResult(
        score = score,
        matchedKeywords = matched,
        missingKeywords = missing
    )
}

// ── Sample data for demo ─────────────────────────────────────────────────────

private const val SAMPLE_JOB_DESCRIPTION = """
We are looking for a Software Engineering Intern with strong skills in Kotlin, Android development, 
and Jetpack Compose. The ideal candidate should have experience with REST APIs, MVVM architecture, 
Room database, Git version control, and unit testing. Familiarity with Hilt for dependency injection 
and Coroutines for async programming is a plus. You will work in an Agile environment and collaborate 
with a cross-functional team to build high-quality mobile applications. 
Strong problem-solving skills and communication are essential.
"""

private const val SAMPLE_RESUME = """
I am a final year Computer Science student with experience building Android applications using Kotlin 
and Jetpack Compose. I have worked with MVVM architecture, Room database, and Retrofit for REST APIs.
Proficient in Git version control, Hilt dependency injection, and Kotlin Coroutines. 
Completed internship at a startup where I built features in an Agile team environment.
Strong analytical and communication skills. Experience with unit testing using JUnit and Mockito.
Built 3 personal Android projects available on GitHub.
"""
