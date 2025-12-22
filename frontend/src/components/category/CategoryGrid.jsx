import { Link } from 'react-router-dom';

export default function CategoryGrid({ categories }) {
  const imageFallback = 'https://picsum.photos/seed/no-image/800/800';

  const categoryImages = {
    electronics: 'https://images.unsplash.com/photo-1498049794561-7780e7231661?fit=crop&w=800&q=80&fm=jpg',
    fashion: 'https://images.unsplash.com/photo-1445205170230-053b83016050?fit=crop&w=800&q=80&fm=jpg',
    home: 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?fit=crop&w=800&q=80&fm=jpg',
    sports: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?fit=crop&w=800&q=80&fm=jpg',
    beauty: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?fit=crop&w=800&q=80&fm=jpg',
    books: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?fit=crop&w=800&q=80&fm=jpg',
  };

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
      {categories.map((category) => (
        <Link
          key={category.id}
          to={`/products?category=${category.id}`}
          className="group"
        >
          <div className="relative aspect-square rounded-2xl overflow-hidden bg-gray-100 dark:bg-gray-700">
            <img
              src={category.image || categoryImages[category.slug] || imageFallback}
              alt={category.name}
              className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              onError={(e) => {
                e.currentTarget.src = imageFallback;
              }}
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-4">
              <h3 className="text-white font-semibold text-center">
                {category.name}
              </h3>
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
}
