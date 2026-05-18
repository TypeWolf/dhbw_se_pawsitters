// Pawsitters — shared species catalog.
// Each entry pairs a free-text species value with the imagery + visual tone we
// render across pet cards / request cards / sitter-job cards.
//
// The Pet entity stores `species` as a free-text String — this catalog only
// powers the UI. Unknown / "Other" values fall back to the paw glyph.

// `aliases` carries the German labels (from i18n) plus common synonyms so a
// species stored in any supported language still resolves to the right photo.
const SPECIES = [
    {
        id: 'dog', label: 'Dog', emoji: '🐕', tone: 'sage',
        aliases: ['hund', 'doggo', 'puppy', 'welpe'],
        photo: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'cat', label: 'Cat', emoji: '🐈', tone: 'peach',
        aliases: ['katze', 'kitten', 'kätzchen'],
        photo: 'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'rabbit', label: 'Rabbit', emoji: '🐇', tone: 'sage',
        aliases: ['hase', 'kaninchen', 'bunny'],
        photo: 'https://images.unsplash.com/photo-1535241749838-299277b6305f?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'hamster', label: 'Hamster', emoji: '🐹', tone: 'peach',
        aliases: ['hamster', 'meerschweinchen', 'guinea pig'],
        photo: 'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'bird', label: 'Bird', emoji: '🐦', tone: 'sage',
        aliases: ['vogel', 'papagei', 'parrot'],
        photo: 'https://images.unsplash.com/photo-1444464666168-49d633b86797?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'fish', label: 'Fish', emoji: '🐟', tone: 'peach',
        aliases: ['fisch'],
        photo: 'https://images.unsplash.com/photo-1535591273668-578e31182c4f?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'reptile', label: 'Reptile', emoji: '🦎', tone: 'sage',
        aliases: ['reptil', 'lizard', 'echse', 'snake', 'schlange'],
        photo: 'https://images.unsplash.com/photo-1591025207163-942350e47db2?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'horse', label: 'Horse', emoji: '🐴', tone: 'peach',
        aliases: ['pferd', 'pony'],
        photo: 'https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?w=400&q=80&auto=format&fit=crop'
    },
    {
        id: 'other', label: 'Other', emoji: '🐾', tone: 'ink',
        aliases: ['anderes', 'andere', 'sonstiges'],
        photo: null
    }
];

/**
 * Resolve any user-typed species string (EN or DE, any casing) to a catalog
 * entry. Tries exact id/label/alias match, then a prefix match across
 * label + aliases, then falls back to "Other".
 */
function findSpecies(input) {
    const s = (input || '').trim().toLowerCase();
    const other = SPECIES[SPECIES.length - 1];
    if (!s) return other;
    const exact = SPECIES.find(x =>
        x.id === s ||
        x.label.toLowerCase() === s ||
        (x.aliases || []).includes(s)
    );
    if (exact) return exact;
    const prefix = SPECIES.find(x =>
        x.label.toLowerCase().startsWith(s) ||
        (x.aliases || []).some(a => a.startsWith(s))
    );
    return prefix || other;
}

/**
 * Render a pet's species visual as a small (or large) tile.
 * - With a photo: returns a div with the photo as the background.
 * - Without (Other / unrecognised): returns the tinted paw-avatar fallback.
 *
 * @param {string} species  raw species string from the Pet record
 * @param {'sm'|'md'|'lg'} size  card size — sm=40, md=64, lg=96
 */
function renderSpeciesTile(species, size = 'md') {
    const s = findSpecies(species);
    const px = size === 'lg' ? 96 : size === 'sm' ? 40 : 64;
    const rad = size === 'lg' ? 18 : size === 'sm' ? 10 : 12;
    if (s.photo) {
        return `<span class="species-tile" style="width:${px}px;height:${px}px;border-radius:${rad}px;background-image:url(${s.photo});background-size:cover;background-position:center;flex-shrink:0;display:inline-block;"></span>`;
    }
    const toneClass = s.tone === 'peach' ? 'avatar-pet-peach'
                    : s.tone === 'ink'   ? 'avatar-pet-ink'
                    : '';
    const sizeClass = size === 'lg' ? 'avatar-pet-lg' : '';
    return `<span class="avatar-pet ${sizeClass} ${toneClass}" style="width:${px}px;height:${px}px;">
        <svg class="icon"><use href="#i-paw"></use></svg>
    </span>`;
}
