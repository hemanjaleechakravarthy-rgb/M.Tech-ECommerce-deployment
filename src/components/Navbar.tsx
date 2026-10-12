import { useAuthStore } from "../store/authStore";
import { useCartStore } from "../store/cartStore";

function Navbar() {

    const { user, logout } = useAuthStore();

    const cartItems = useCartStore(
        (state) => state.cartItems
    );

    const numberOfItems = cartItems.reduce(
        (sum, item) => sum + item.quantity,
        0
    );

    const goTo = (path: string) => {
        window.history.pushState({}, "", path);
        window.dispatchEvent(
            new PopStateEvent("popstate")
        );
    };

    return (
        <nav className="navbar">

            <div className="navbar-brand">
                SmartCart
            </div>

            <div className="navbar-links">

                <button onClick={() => goTo("/")}>
                    Home
                </button>

                <button onClick={() => goTo("/user")}>
                    🛒 Cart ({numberOfItems})
                </button>

                {!user && (
                    <>
                        <button
                            onClick={() => goTo("/signin")}
                        >
                            Sign In
                        </button>

                        <button
                            onClick={() => goTo("/signup")}
                        >
                            Sign Up
                        </button>
                    </>
                )}

                {user && user.role === "ADMIN" && (
                    <button
                        onClick={() => goTo("/admin")}
                    >
                        Admin Dashboard
                    </button>
                )}

                {user && (
                    <>
                        <span className="welcome-user">
                            Welcome, {user.name}
                        </span>

                        <button
                            onClick={async () => {
                                await logout();
                                goTo("/signin");
                            }}
                        >
                            Logout
                        </button>
                    </>
                )}

            </div>

        </nav>
    );
}

export default Navbar;