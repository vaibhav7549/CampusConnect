-- ═══════════════════════════════════════════════════════════════
-- CampusConnect — Complete Supabase Setup
-- Run this ENTIRE script in  Supabase Dashboard → SQL Editor
-- ═══════════════════════════════════════════════════════════════

-- ── 1. PROFILES TABLE ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT,
    college TEXT,
    branch TEXT,
    year INT,
    college_email TEXT,
    photo_url TEXT DEFAULT 'https://ui-avatars.com/api/?background=6C5CE7&color=ffffff&name=Campus+Connect',
    bio TEXT DEFAULT '',
    is_verified BOOLEAN DEFAULT false,
    fcm_token TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Ensure all new columns exist for older profiles tables
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS bio TEXT DEFAULT '';
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS photo_url TEXT DEFAULT 'https://ui-avatars.com/api/?background=6C5CE7&color=ffffff&name=Campus+Connect';
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS fcm_token TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS college TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS branch TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS year INT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS college_email TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Profiles are viewable by everyone" ON profiles;
CREATE POLICY "Profiles are viewable by everyone" ON profiles FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);
DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);

-- ── 2. POSTS TABLE ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    subject TEXT,
    branch TEXT,
    image_url TEXT,
    pdf_url TEXT,
    upvotes INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Ensure all new columns exist for older posts tables
ALTER TABLE posts ADD COLUMN IF NOT EXISTS subject TEXT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS branch TEXT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS pdf_url TEXT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS upvotes INT DEFAULT 0;

ALTER TABLE posts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Posts are viewable by everyone" ON posts;
CREATE POLICY "Posts are viewable by everyone" ON posts FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can create posts" ON posts;
CREATE POLICY "Users can create posts" ON posts FOR INSERT WITH CHECK (auth.uid() = author_id);
DROP POLICY IF EXISTS "Users can delete own posts" ON posts;
CREATE POLICY "Users can delete own posts" ON posts FOR DELETE USING (auth.uid() = author_id);
DROP POLICY IF EXISTS "Users can update own posts" ON posts;
CREATE POLICY "Users can update own posts" ON posts FOR UPDATE USING (auth.uid() = author_id);

-- ── 3. POST UPVOTES ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS post_upvotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(post_id, user_id)
);

ALTER TABLE post_upvotes ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Upvotes viewable by everyone" ON post_upvotes;
CREATE POLICY "Upvotes viewable by everyone" ON post_upvotes FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can upvote" ON post_upvotes;
CREATE POLICY "Users can upvote" ON post_upvotes FOR INSERT WITH CHECK (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can remove own upvote" ON post_upvotes;
CREATE POLICY "Users can remove own upvote" ON post_upvotes FOR DELETE USING (auth.uid() = user_id);

-- ── 4. BOOKMARKS ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(post_id, user_id)
);

ALTER TABLE bookmarks ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can view own bookmarks" ON bookmarks;
CREATE POLICY "Users can view own bookmarks" ON bookmarks FOR SELECT USING (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can bookmark" ON bookmarks;
CREATE POLICY "Users can bookmark" ON bookmarks FOR INSERT WITH CHECK (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can remove own bookmark" ON bookmarks;
CREATE POLICY "Users can remove own bookmark" ON bookmarks FOR DELETE USING (auth.uid() = user_id);

-- ── 5. COMMENTS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS parent_id UUID REFERENCES comments(id) ON DELETE CASCADE;
DROP POLICY IF EXISTS "Comments viewable by everyone" ON comments;
CREATE POLICY "Comments viewable by everyone" ON comments FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can comment" ON comments;
CREATE POLICY "Users can comment" ON comments FOR INSERT WITH CHECK (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can delete own comments" ON comments;
CREATE POLICY "Users can delete own comments" ON comments FOR DELETE USING (auth.uid() = user_id);

-- ── 6. FRIEND REQUESTS ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS friend_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_uid UUID REFERENCES profiles(id) ON DELETE CASCADE,
    to_uid UUID REFERENCES profiles(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected')),
    sent_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(from_uid, to_uid)
);

ALTER TABLE friend_requests ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can view own requests" ON friend_requests;
CREATE POLICY "Users can view own requests" ON friend_requests FOR SELECT
    USING (auth.uid() = from_uid OR auth.uid() = to_uid);
DROP POLICY IF EXISTS "Users can send requests" ON friend_requests;
CREATE POLICY "Users can send requests" ON friend_requests FOR INSERT
    WITH CHECK (auth.uid() = from_uid);
DROP POLICY IF EXISTS "Users can update received requests" ON friend_requests;
CREATE POLICY "Users can update received requests" ON friend_requests FOR UPDATE
    USING (auth.uid() = to_uid);

-- ── 7. FRIENDS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS friends (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    friend_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, friend_id)
);

ALTER TABLE friends ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can view own friends" ON friends;
CREATE POLICY "Users can view own friends" ON friends FOR SELECT
    USING (auth.uid() = user_id OR auth.uid() = friend_id);
DROP POLICY IF EXISTS "Users can add friends" ON friends;
CREATE POLICY "Users can add friends" ON friends FOR INSERT
    WITH CHECK (auth.uid() = user_id OR auth.uid() = friend_id);

-- ── 8. CHAT ROOMS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user1_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    user2_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    last_message TEXT,
    last_message_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user1_id, user2_id)
);

ALTER TABLE chat_rooms ENABLE ROW LEVEL SECURITY;
DO $$
DECLARE
    p RECORD;
BEGIN
    FOR p IN
        SELECT policyname
        FROM pg_policies
        WHERE schemaname = 'public'
          AND tablename = 'chat_rooms'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.chat_rooms', p.policyname);
    END LOOP;
END $$;
DROP POLICY IF EXISTS "Users can view own chats" ON chat_rooms;
CREATE POLICY "Users can view own chats" ON chat_rooms FOR SELECT
    USING (auth.uid() = user1_id OR auth.uid() = user2_id);
DROP POLICY IF EXISTS "Users can create chats" ON chat_rooms;
CREATE POLICY "Users can create chats" ON chat_rooms FOR INSERT
    WITH CHECK (
        auth.uid() IS NOT NULL
        AND auth.uid() IN (user1_id, user2_id)
        AND user1_id <> user2_id
    );
DROP POLICY IF EXISTS "Users can update own chats" ON chat_rooms;
CREATE POLICY "Users can update own chats" ON chat_rooms FOR UPDATE
    USING (auth.uid() = user1_id OR auth.uid() = user2_id);

CREATE OR REPLACE FUNCTION public.normalize_chat_room_users()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    temp_user UUID;
BEGIN
    IF NEW.user1_id IS NULL OR NEW.user2_id IS NULL THEN
        RAISE EXCEPTION 'chat room participants are required';
    END IF;

    IF NEW.user1_id = NEW.user2_id THEN
        RAISE EXCEPTION 'chat room participants must be different';
    END IF;

    IF NEW.user1_id::text > NEW.user2_id::text THEN
        temp_user := NEW.user1_id;
        NEW.user1_id := NEW.user2_id;
        NEW.user2_id := temp_user;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_chat_rooms_normalize_users ON chat_rooms;
CREATE TRIGGER trg_chat_rooms_normalize_users
BEFORE INSERT ON chat_rooms
FOR EACH ROW
EXECUTE FUNCTION public.normalize_chat_room_users();

-- ── 9. MESSAGES ───────────────────────────────────────────────
DROP TABLE IF EXISTS messages CASCADE;
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    content TEXT,
    image_url TEXT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT messages_content_or_image_check CHECK (
        COALESCE(NULLIF(trim(content), ''), '') <> '' OR image_url IS NOT NULL
    )
);

ALTER TABLE messages ALTER COLUMN content DROP NOT NULL;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS image_url TEXT;

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can view messages in their rooms" ON messages;
CREATE POLICY "Users can view messages in their rooms" ON messages FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM chat_rooms WHERE chat_rooms.id = messages.room_id
        AND (chat_rooms.user1_id = auth.uid() OR chat_rooms.user2_id = auth.uid())
    ));
DROP POLICY IF EXISTS "Users can send messages" ON messages;
CREATE POLICY "Users can send messages" ON messages FOR INSERT
    WITH CHECK (auth.uid() = sender_id);
DROP POLICY IF EXISTS "Users can update messages" ON messages;
CREATE POLICY "Users can update messages" ON messages FOR UPDATE
    USING (EXISTS (
        SELECT 1 FROM chat_rooms WHERE chat_rooms.id = messages.room_id
        AND (chat_rooms.user1_id = auth.uid() OR chat_rooms.user2_id = auth.uid())
    ));

-- ── 10. TODOS ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS todos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    is_completed BOOLEAN DEFAULT false,
    due_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE todos ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can manage own todos" ON todos;
CREATE POLICY "Users can manage own todos" ON todos FOR ALL USING (auth.uid() = user_id);

-- ── 11. EXAMS ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS exams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    subject TEXT NOT NULL,
    exam_date TIMESTAMPTZ NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE exams ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can manage own exams" ON exams;
CREATE POLICY "Users can manage own exams" ON exams FOR ALL USING (auth.uid() = user_id);

-- ── 12. LISTINGS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    price NUMERIC DEFAULT 0,
    category TEXT,
    image_url TEXT,
    is_sold BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE listings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Listings viewable by everyone" ON listings;
CREATE POLICY "Listings viewable by everyone" ON listings FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can create listings" ON listings;
CREATE POLICY "Users can create listings" ON listings FOR INSERT WITH CHECK (auth.uid() = seller_id);
DROP POLICY IF EXISTS "Users can update own listings" ON listings;
CREATE POLICY "Users can update own listings" ON listings FOR UPDATE USING (auth.uid() = seller_id);
DROP POLICY IF EXISTS "Users can delete own listings" ON listings;
CREATE POLICY "Users can delete own listings" ON listings FOR DELETE USING (auth.uid() = seller_id);

-- ── 13. OPPORTUNITIES ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS opportunities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    posted_by UUID REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    type TEXT CHECK (type IN ('internship', 'project', 'hackathon', 'other')),
    apply_link TEXT,
    deadline TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE opportunities ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Opportunities viewable by everyone" ON opportunities;
CREATE POLICY "Opportunities viewable by everyone" ON opportunities FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can post opportunities" ON opportunities;
CREATE POLICY "Users can post opportunities" ON opportunities FOR INSERT WITH CHECK (auth.uid() = posted_by);
DROP POLICY IF EXISTS "Users can delete own opportunities" ON opportunities;
CREATE POLICY "Users can delete own opportunities" ON opportunities FOR DELETE USING (auth.uid() = posted_by);

-- ── 14. NOTIFICATIONS ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    message TEXT NOT NULL,
    from_uid UUID,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Users can view own notifications" ON notifications;
CREATE POLICY "Users can view own notifications" ON notifications FOR SELECT USING (auth.uid() = user_id);
DROP POLICY IF EXISTS "Anyone can create notifications" ON notifications;
CREATE POLICY "Anyone can create notifications" ON notifications FOR INSERT WITH CHECK (true);
DROP POLICY IF EXISTS "Users can update own notifications" ON notifications;
CREATE POLICY "Users can update own notifications" ON notifications FOR UPDATE USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════
-- TRIGGER: Auto-create profile on signup
-- ═══════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, name, bio, created_at)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1)),
        'Hey there! I''m using CampusConnect 🎓',
        now()
    )
    ON CONFLICT (id) DO NOTHING;

    UPDATE public.profiles
    SET photo_url = COALESCE(
        photo_url,
        'https://ui-avatars.com/api/?background=6C5CE7&color=ffffff&name=' ||
        replace(COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1)), ' ', '+')
    )
    WHERE id = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ═══════════════════════════════════════════════════════════════
-- DONE! Tables, RLS policies, and auto-profile trigger created.
-- Now you need to create Storage Buckets via Supabase Dashboard:
--   1. avatars (public)
--   2. post-images (public)
--   3. post-pdfs (public)
--   4. listing-images (public)
-- ═══════════════════════════════════════════════════════════════
--   5. lost-item-images (public) [optional if image upload is added]
--   6. chat-images (public)
-- ═══════════════════════════════════════════════════════════════

-- ── 15. LOST ITEMS (SOCIAL SAFETY BOARD) ───────────────────────
CREATE TABLE IF NOT EXISTS lost_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    location TEXT NOT NULL,
    contact TEXT NOT NULL,
    category TEXT,
    image_url TEXT,
    is_found BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE lost_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Lost items viewable by everyone" ON lost_items;
CREATE POLICY "Lost items viewable by everyone" ON lost_items FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can create lost items" ON lost_items;
CREATE POLICY "Users can create lost items" ON lost_items FOR INSERT WITH CHECK (auth.uid() = owner_id);
DROP POLICY IF EXISTS "Users can update own lost items" ON lost_items;
CREATE POLICY "Users can update own lost items" ON lost_items FOR UPDATE USING (auth.uid() = owner_id);
DROP POLICY IF EXISTS "Users can delete own lost items" ON lost_items;
CREATE POLICY "Users can delete own lost items" ON lost_items FOR DELETE USING (auth.uid() = owner_id);

-- ── 16. STORAGE BUCKETS + POLICIES ─────────────────────────────
INSERT INTO storage.buckets (id, name, public)
VALUES
    ('avatars', 'avatars', true),
    ('post-images', 'post-images', true),
    ('post-pdfs', 'post-pdfs', true),
    ('listing-images', 'listing-images', true),
    ('lost-item-images', 'lost-item-images', true),
    ('chat-images', 'chat-images', true)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "Public can view media objects" ON storage.objects;
CREATE POLICY "Public can view media objects"
ON storage.objects FOR SELECT
USING (bucket_id IN ('avatars', 'post-images', 'post-pdfs', 'listing-images', 'lost-item-images', 'chat-images'));

DROP POLICY IF EXISTS "Authenticated users can upload media objects" ON storage.objects;
CREATE POLICY "Authenticated users can upload media objects"
ON storage.objects FOR INSERT
WITH CHECK (
    auth.role() = 'authenticated'
    AND bucket_id IN ('avatars', 'post-images', 'post-pdfs', 'listing-images', 'lost-item-images', 'chat-images')
);

DROP POLICY IF EXISTS "Authenticated users can update media objects" ON storage.objects;
CREATE POLICY "Authenticated users can update media objects"
ON storage.objects FOR UPDATE
USING (
    auth.role() = 'authenticated'
    AND bucket_id IN ('avatars', 'post-images', 'post-pdfs', 'listing-images', 'lost-item-images', 'chat-images')
)
WITH CHECK (
    auth.role() = 'authenticated'
    AND bucket_id IN ('avatars', 'post-images', 'post-pdfs', 'listing-images', 'lost-item-images', 'chat-images')
);
