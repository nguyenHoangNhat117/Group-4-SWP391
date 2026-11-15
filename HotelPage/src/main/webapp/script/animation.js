export default function addObserver() {
    const cards = document.querySelectorAll(".card");
    const texts = document.querySelectorAll(".text");

    const cardObserver = new IntersectionObserver(
            (entries) => {
        entries.forEach((entry) =>
            entry.target.classList.toggle("hidden", !entry.isIntersecting)
        );
    },
            {
                threshold: 0.4
            }
    );

    const textObserver = new IntersectionObserver(
            (entries) => {
        entries.forEach((entry) =>
            entry.target.classList.toggle("hidden", !entry.isIntersecting)
        );
    },
            {
                threshold: 0.4
            }
    );

    cards.forEach((card) => cardObserver.observe(card));
    texts.forEach((text) => textObserver.observe(text));
}
