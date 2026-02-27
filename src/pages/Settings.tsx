import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { useToast } from "@/hooks/use-toast";

const Settings = () => {
  const { user, updateProfile } = useAuth();
  const { toast } = useToast();
  const [name, setName] = useState(user?.name || "");
  const [email, setEmail] = useState(user?.email || "");
  const [darkMode, setDarkMode] = useState(user?.darkMode || false);

  const handleSave = async () => {
    await updateProfile({ name, email });
    toast({ title: "Saved", description: "Profile updated successfully" });
  };

  const handleDarkMode = (checked: boolean) => {
    setDarkMode(checked);
    document.documentElement.classList.toggle("dark", checked);
    updateProfile({ darkMode: checked });
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-2xl">
      <h1 className="text-2xl font-bold font-display">Settings</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Profile</CardTitle>
          <CardDescription className="text-xs">Update your personal details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <Label className="text-xs">Full Name</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label className="text-xs">Email</Label>
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <Button onClick={handleSave} size="sm">Save Changes</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Password</CardTitle>
          <CardDescription className="text-xs">Change your account password</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1"><Label className="text-xs">Current Password</Label><Input type="password" /></div>
          <div className="space-y-1"><Label className="text-xs">New Password</Label><Input type="password" /></div>
          <Button size="sm" variant="outline" onClick={() => toast({ title: "Coming Soon", description: "Password change requires backend" })}>
            Update Password
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Preferences</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Dark Mode</p>
              <p className="text-xs text-muted-foreground">Toggle dark theme</p>
            </div>
            <Switch checked={darkMode} onCheckedChange={handleDarkMode} />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Currency Symbol</p>
              <p className="text-xs text-muted-foreground">Currently: ₹ (INR)</p>
            </div>
            <Button variant="outline" size="sm" disabled>₹ INR</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Settings;
