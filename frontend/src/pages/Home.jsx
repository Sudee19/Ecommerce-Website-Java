import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, TruckIcon, ShieldCheckIcon, CreditCardIcon } from '@heroicons/react/24/outline';
import ProductCard from '../components/product/ProductCard';
import CategoryGrid from '../components/category/CategoryGrid';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { productsAPI, categoriesAPI } from '../services/api';

export default function Home() {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [newArrivals, setNewArrivals] = useState([]);
  const [topRated, setTopRated] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const [dealEndsAt] = useState(() => Date.now() + 6 * 60 * 60 * 1000);
  const [dealTimeLeft, setDealTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 });

  useEffect(() => {
    const getTimeLeft = () => {
      const diff = Math.max(0, dealEndsAt - Date.now());
      const totalSeconds = Math.floor(diff / 1000);
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;
      return { hours, minutes, seconds };
    };

    setDealTimeLeft(getTimeLeft());
    const id = setInterval(() => {
      setDealTimeLeft(getTimeLeft());
    }, 1000);
    return () => clearInterval(id);
  }, [dealEndsAt]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [productsRes, categoriesRes, newArrivalsRes, topRatedRes] = await Promise.all([
          productsAPI.getFeatured({ size: 8 }),
          categoriesAPI.getRoot(),
          productsAPI.getAll({ size: 8, sortBy: 'createdAt', sortDir: 'desc' }),
          productsAPI.getAll({ size: 8, sortBy: 'averageRating', sortDir: 'desc' }),
        ]);
        setFeaturedProducts(productsRes.data.data.content || []);
        setCategories(categoriesRes.data.data || []);
        setNewArrivals(newArrivalsRes.data.data.content || []);
        setTopRated(topRatedRes.data.data.content || []);
      } catch (error) {
        console.error('Failed to fetch data:', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const features = [
    {
      icon: TruckIcon,
      title: 'Free Shipping',
      description: 'Free shipping on orders over ₹500',
    },
    {
      icon: ShieldCheckIcon,
      title: 'Secure Payment',
      description: '100% secure payment gateway',
    },
    {
      icon: CreditCardIcon,
      title: 'Easy Returns',
      description: '30-day return policy',
    },
  ];

  const brands = [
    'ShopEase',
    'Nova',
    'Urban',
    'Pulse',
    'Aura',
    'Vertex',
  ];

  const testimonials = [
    {
      name: 'Aarav Sharma',
      role: 'Verified Buyer',
      quote: 'Fast delivery and the quality is genuinely premium. The UI is super smooth too.',
      rating: 5,
      avatar: 'https://picsum.photos/seed/testimonial-1/96/96',
    },
    {
      name: 'Isha Verma',
      role: 'Repeat Customer',
      quote: 'Loved the discounts and the checkout experience. Clean design and great product variety.',
      rating: 5,
      avatar: 'https://picsum.photos/seed/testimonial-2/96/96',
    },
    {
      name: 'Kabir Singh',
      role: 'Fitness Enthusiast',
      quote: 'The deals are insane. Found everything I needed in one place.',
      rating: 4,
      avatar: 'https://picsum.photos/seed/testimonial-3/96/96',
    },
  ];

  const deals = featuredProducts
    .filter((p) => (p.discountPercentage || 0) > 0)
    .slice(0, 4);

  const pad2 = (n) => String(n).padStart(2, '0');

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-r from-primary-600 via-accent-500 to-primary-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-32">
          <div className="max-w-2xl">
            <h1 className="text-4xl md:text-6xl font-bold mb-6 animate-slide-up">
              Discover Amazing Products
            </h1>
            <p className="text-lg md:text-xl text-primary-100 mb-8 animate-slide-up">
              Shop the latest trends with unbeatable prices. Quality products delivered to your doorstep.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 animate-slide-up">
              <Link
                to="/products"
                className="inline-flex items-center justify-center px-8 py-3 bg-white text-primary-600 font-semibold rounded-lg hover:bg-gray-100 transition-colors"
              >
                Shop Now
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
              <Link
                to="/products?featured=true"
                className="inline-flex items-center justify-center px-8 py-3 border-2 border-white text-white font-semibold rounded-lg hover:bg-white/10 transition-colors"
              >
                View Featured
              </Link>
            </div>
          </div>
        </div>
        
        {/* Decorative Elements */}
        <div className="absolute top-0 right-0 w-1/3 h-full bg-white/5 -skew-x-12 hidden lg:block" />
      </section>

      <section className="py-16 bg-gray-50 dark:bg-gray-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="rounded-3xl border border-gray-200 dark:border-gray-800 overflow-hidden bg-white dark:bg-gray-900">
            <div className="p-6 md:p-8 bg-gradient-to-r from-primary-600 via-accent-500 to-primary-800 text-white">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                <div>
                  <h2 className="text-2xl md:text-3xl font-bold">Flash Deals</h2>
                  <p className="text-primary-100 mt-2">Limited-time offers on trending picks. Grab them before they’re gone.</p>
                </div>
                <div className="flex items-center gap-3">
                  <div className="bg-white/15 rounded-2xl px-4 py-3 text-center">
                    <div className="text-2xl font-extrabold tabular-nums">{pad2(dealTimeLeft.hours)}</div>
                    <div className="text-xs text-primary-100">Hours</div>
                  </div>
                  <div className="bg-white/15 rounded-2xl px-4 py-3 text-center">
                    <div className="text-2xl font-extrabold tabular-nums">{pad2(dealTimeLeft.minutes)}</div>
                    <div className="text-xs text-primary-100">Minutes</div>
                  </div>
                  <div className="bg-white/15 rounded-2xl px-4 py-3 text-center">
                    <div className="text-2xl font-extrabold tabular-nums">{pad2(dealTimeLeft.seconds)}</div>
                    <div className="text-xs text-primary-100">Seconds</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="p-6 md:p-8">
              {isLoading ? (
                <div className="flex justify-center py-12">
                  <LoadingSpinner size="lg" />
                </div>
              ) : deals.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
                  {deals.map((product) => (
                    <ProductCard key={product.id} product={product} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-10">
                  <p className="text-gray-600 dark:text-gray-300">No deals available right now.</p>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Add discount prices in demo data to show Flash Deals.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      <section className="py-10 border-y border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
            {brands.map((b) => (
              <div
                key={b}
                className="rounded-2xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900/60 px-4 py-4 flex items-center justify-center"
              >
                <span className="text-sm font-semibold text-gray-700 dark:text-gray-200">
                  {b}
                </span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="bg-white dark:bg-gray-800 py-12 border-b dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <div key={index} className="flex items-center gap-4">
                <div className="flex-shrink-0 w-12 h-12 bg-primary-100 dark:bg-primary-900 rounded-lg flex items-center justify-center">
                  <feature.icon className="h-6 w-6 text-primary-600 dark:text-primary-400" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900 dark:text-white">{feature.title}</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400">{feature.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
              New Arrivals
            </h2>
            <Link
              to="/products?sortBy=createdAt&sortDir=desc"
              className="text-primary-600 hover:text-primary-700 font-medium flex items-center"
            >
              View All
              <ArrowRightIcon className="ml-1 h-4 w-4" />
            </Link>
          </div>

          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : newArrivals.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {newArrivals.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-12">No new arrivals available</p>
          )}
        </div>
      </section>

      <section className="py-16 bg-gray-50 dark:bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
              Top Rated
            </h2>
            <Link
              to="/products?sortBy=averageRating&sortDir=desc"
              className="text-primary-600 hover:text-primary-700 font-medium flex items-center"
            >
              View All
              <ArrowRightIcon className="ml-1 h-4 w-4" />
            </Link>
          </div>

          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : topRated.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {topRated.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-12">No top rated products available</p>
          )}
        </div>
      </section>

      {/* Categories */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
              Shop by Category
            </h2>
            <Link
              to="/products"
              className="text-primary-600 hover:text-primary-700 font-medium flex items-center"
            >
              View All
              <ArrowRightIcon className="ml-1 h-4 w-4" />
            </Link>
          </div>
          
          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : categories.length > 0 ? (
            <CategoryGrid categories={categories} />
          ) : (
            <p className="text-center text-gray-500 py-12">No categories available</p>
          )}
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-16 bg-gray-50 dark:bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
              Featured Products
            </h2>
            <Link
              to="/products?featured=true"
              className="text-primary-600 hover:text-primary-700 font-medium flex items-center"
            >
              View All
              <ArrowRightIcon className="ml-1 h-4 w-4" />
            </Link>
          </div>
          
          {isLoading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : featuredProducts.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {featuredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-12">No featured products available</p>
          )}
        </div>
      </section>

      <section className="py-16 bg-white dark:bg-gray-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
              Loved by shoppers
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {testimonials.map((t) => (
              <div key={t.name} className="card-hover">
                <div className="flex items-center gap-4">
                  <img src={t.avatar} alt={t.name} className="w-12 h-12 rounded-full object-cover" loading="lazy" />
                  <div>
                    <div className="font-semibold text-gray-900 dark:text-white">{t.name}</div>
                    <div className="text-sm text-gray-500 dark:text-gray-400">{t.role}</div>
                  </div>
                </div>
                <p className="mt-4 text-gray-700 dark:text-gray-300">“{t.quote}”</p>
                <div className="mt-4 text-sm text-primary-600 dark:text-primary-400 font-semibold">
                  {'★'.repeat(t.rating)}{'☆'.repeat(5 - t.rating)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Newsletter */}
      <section className="py-16 bg-primary-600">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-2xl md:text-3xl font-bold text-white mb-4">
            Subscribe to Our Newsletter
          </h2>
          <p className="text-primary-100 mb-8 max-w-2xl mx-auto">
            Get the latest updates on new products and upcoming sales.
          </p>
          <form className="max-w-md mx-auto flex gap-2">
            <input
              type="email"
              placeholder="Enter your email"
              className="flex-1 px-4 py-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-white"
            />
            <button
              type="submit"
              className="px-6 py-3 bg-gray-900 text-white font-semibold rounded-lg hover:bg-gray-800 transition-colors"
            >
              Subscribe
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
