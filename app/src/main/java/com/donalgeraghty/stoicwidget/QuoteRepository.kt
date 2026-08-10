package com.donalgeraghty.stoicwidget

import java.time.LocalDateTime
import java.time.ZoneId

object QuoteRepository {
    // Concise adaptations of ideas from classical Stoic texts. Keeping these
    // local makes the widget fast, private, offline, and free of API costs.
    private val quotes = listOf(
        Quote("What stands in the way becomes the way.", "Marcus Aurelius — adapted"),
        Quote("Your mind is yours; outside events are not.", "Marcus Aurelius — adapted"),
        Quote("The quality of your thoughts shapes the quality of your life.", "Marcus Aurelius — adapted"),
        Quote("Waste no more time arguing what a good person should be. Be one.", "Marcus Aurelius — adapted"),
        Quote("Choose not to feel harmed, and the harm loses its hold.", "Marcus Aurelius — adapted"),
        Quote("You can meet this moment with reason and good character.", "Marcus Aurelius — adapted"),
        Quote("Do the work before you; let the rest unfold as it will.", "Marcus Aurelius — adapted"),
        Quote("Receive without pride. Let go without attachment.", "Marcus Aurelius — adapted"),
        Quote("The present moment is enough for the work of a lifetime.", "Marcus Aurelius — adapted"),
        Quote("Be tolerant with others and strict with yourself.", "Marcus Aurelius — adapted"),
        Quote("A calm mind is a stronger fortress than anger.", "Marcus Aurelius — adapted"),
        Quote("If it is not right, do not do it; if it is not true, do not say it.", "Marcus Aurelius — adapted"),
        Quote("You become like the thoughts you return to most often.", "Marcus Aurelius — adapted"),
        Quote("Look closely: most troubles shrink when stripped of opinion.", "Marcus Aurelius — adapted"),
        Quote("Act as though the task in front of you truly matters.", "Marcus Aurelius — adapted"),
        Quote("Accept what arrives; use it well.", "Marcus Aurelius — adapted"),
        Quote("No one can prevent you from acting with courage and justice.", "Marcus Aurelius — adapted"),
        Quote("Do not demand that life happen as you wish; work with what happens.", "Epictetus — adapted"),
        Quote("Some things are up to us, and some are not.", "Epictetus — adapted"),
        Quote("Freedom begins with knowing what is in your control.", "Epictetus — adapted"),
        Quote("It is not events that disturb us, but our judgments about them.", "Epictetus — adapted"),
        Quote("First decide who you want to be; then do what follows.", "Epictetus — adapted"),
        Quote("No great thing is created suddenly.", "Epictetus — adapted"),
        Quote("Difficulties reveal what a person has practiced.", "Epictetus — adapted"),
        Quote("If you want to improve, be willing to look foolish for a while.", "Epictetus — adapted"),
        Quote("Seek to master your desires rather than the world.", "Epictetus — adapted"),
        Quote("A person is free when their choices are their own.", "Epictetus — adapted"),
        Quote("Use what happens as material for character.", "Epictetus — adapted"),
        Quote("When provoked, remember that your judgment adds the sting.", "Epictetus — adapted"),
        Quote("Practice in small things before the hard test arrives.", "Epictetus — adapted"),
        Quote("Do not explain your philosophy. Embody it.", "Epictetus — adapted"),
        Quote("Wanting less is often easier than acquiring more.", "Epictetus — adapted"),
        Quote("Ask not what will happen, but how you will meet it.", "Epictetus — adapted"),
        Quote("Your role is yours to play well; the casting is not yours.", "Epictetus — adapted"),
        Quote("Make the best use of what is in your power.", "Epictetus — adapted"),
        Quote("Luck is what happens; character is what you do with it.", "Epictetus — adapted"),
        Quote("We suffer more often in imagination than in reality.", "Seneca — adapted"),
        Quote("Life is long enough when it is used well.", "Seneca — adapted"),
        Quote("While we wait for life, life passes.", "Seneca — adapted"),
        Quote("Begin at once to live, and count each day as a life.", "Seneca — adapted"),
        Quote("No person is free who is a slave to appetite.", "Seneca — adapted"),
        Quote("A setback can train the very strength it tests.", "Seneca — adapted"),
        Quote("The greatest remedy for anger is delay.", "Seneca — adapted"),
        Quote("Associate with people who make you better.", "Seneca — adapted"),
        Quote("He who needs riches least enjoys them most.", "Seneca — adapted"),
        Quote("Fear often hurts us before the thing we fear ever does.", "Seneca — adapted"),
        Quote("Difficulties strengthen the mind as labor strengthens the body.", "Seneca — adapted"),
        Quote("It is better to conquer grief than to deceive it.", "Seneca — adapted"),
        Quote("Every new beginning comes from some other beginning's end.", "Seneca — adapted"),
        Quote("A good life is measured by its direction, not its length.", "Seneca — adapted"),
        Quote("Enjoy present pleasures without depending on them.", "Seneca — adapted"),
        Quote("If a person knows no destination, no wind is favorable.", "Seneca — adapted"),
        Quote("Nothing is burdensome if the mind does not make it so.", "Seneca — adapted"),
        Quote("Train for uncertainty while times are easy.", "Seneca — adapted"),
        Quote("Possessions are safest when you can lose them calmly.", "Seneca — adapted"),
        Quote("Keep death in view, and trivial fears lose their size.", "Seneca — adapted"),
        Quote("Character is built in ordinary decisions.", "Musonius Rufus — adapted"),
        Quote("Philosophy should change how you live, not merely how you speak.", "Musonius Rufus — adapted"),
        Quote("Practice endurance before comfort becomes a requirement.", "Musonius Rufus — adapted"),
        Quote("Self-control is strength exercised quietly.", "Musonius Rufus — adapted"),
        Quote("Simple living leaves more room for good living.", "Musonius Rufus — adapted"),
        Quote("Train the body as an ally of a disciplined mind.", "Musonius Rufus — adapted"),
        Quote("Courage grows by doing what courage requires.", "Musonius Rufus — adapted"),
        Quote("Learning matters when it becomes action.", "Musonius Rufus — adapted"),
        Quote("Endurance is easier when the purpose is clear.", "Musonius Rufus — adapted"),
        Quote("Choose usefulness over display.", "Musonius Rufus — adapted"),
        Quote("The good person needs practice as much as instruction.", "Musonius Rufus — adapted"),
        Quote("Meet luxury with independence, not resentment.", "Musonius Rufus — adapted"),
        Quote("A disciplined life is not deprivation; it is freedom from excess.", "Musonius Rufus — adapted"),
        Quote("What you repeatedly do becomes part of your character.", "Stoic principle — adapted"),
        Quote("Pause between the event and your judgment of it.", "Stoic principle — adapted"),
        Quote("Control the response, not the weather.", "Stoic principle — adapted")
    )

    fun quoteForCurrentHour(zoneId: ZoneId = ZoneId.systemDefault()): Quote {
        val now = LocalDateTime.now(zoneId)
        // Same quote throughout a clock-hour, deterministic across widget refreshes.
        val hourKey = now.toLocalDate().toEpochDay() * 24L + now.hour
        val index = Math.floorMod(hourKey, quotes.size.toLong()).toInt()
        return quotes[index]
    }

    fun nextQuote(current: Quote): Quote {
        val currentIndex = quotes.indexOf(current).takeIf { it >= 0 } ?: 0
        return quotes[(currentIndex + 1) % quotes.size]
    }

    fun size(): Int = quotes.size
}
