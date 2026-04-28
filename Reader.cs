
namespace lightning323.packInstaller.IO;

public class Reader
{
    private static readonly HttpClient client = new HttpClient();

    public static async Task<string> ReadFile(Uri packTomlURL,
    string file, string hashFormat, string hash)
    {
        Uri fileUri = new Uri(packTomlURL, file);
        string result = await client.GetStringAsync(packTomlURL);
        Console.WriteLine(result);
        return result;
    }
}